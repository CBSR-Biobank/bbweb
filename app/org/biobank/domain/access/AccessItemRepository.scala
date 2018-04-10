package org.biobank.domain.access

import com.google.inject.ImplementedBy
import javax.inject.{Inject, Singleton}
import org.biobank.{Global, TestData}
import org.biobank.domain._
import org.biobank.domain.access.PermissionId._
import org.biobank.domain.access.RoleId._
import org.biobank.domain.users.UserId
import org.slf4j.{Logger, LoggerFactory}
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

@ImplementedBy(classOf[AccessItemRepositoryImpl])
trait AccessItemRepository extends ReadWriteRepositoryWithSlug[AccessItemId, AccessItem] {

  def getRole(id: AccessItemId): DomainValidation[Role]

  def getRoles(): Set[Role]

  def rolesForUser(userId: UserId): Set[Role]

  def getPermission(id: AccessItemId): DomainValidation[Permission]

}

@Singleton
class AccessItemRepositoryImpl @Inject() (val testData: TestData)
    extends ReadWriteRepositoryRefImplWithSlug[AccessItemId, AccessItem](v => v.id)
    with AccessItemRepository {

  import org.biobank.CommonValidations._
  import org.biobank.domain.access.AccessItem._

  val log: Logger = LoggerFactory.getLogger(this.getClass)

  def nextIdentity: AccessItemId = new AccessItemId(nextIdentityAsString)

  protected def notFound(id: AccessItemId): IdNotFound = IdNotFound(s"access item id: $id")

  protected def slugNotFound(slug: String): EntityCriteriaNotFound =
    EntityCriteriaNotFound(s"access item slug: $slug")

  override def init(): Unit = {
    super.init()
    initPermissions
    initRoles
    testData.testRoles.foreach { case (userId, roleId) =>
      getRole(roleId).foreach { role =>
        put(role.copy(userIds = role.userIds + userId))
      }
    }
  }

  def getRole(id: AccessItemId): DomainValidation[Role] = {
    for {
      accessItem <- getByKey(id).leftMap(err => IdNotFound{s"role id: $id"}.nel)
      role <- {
        accessItem match {
          case role: Role => role.successNel[String]
          case _ => EntityCriteriaError(s"access item not a role: $id").failureNel[Role]
        }
      }
    } yield role
  }

  def getRoles(): Set[Role] = {
    getValues.collect { case r: Role => r }.toSet
  }

  def rolesForUser(userId: UserId): Set[Role] = {
    getValues
      .collect { case r: Role => r }
      .filter { r => r.userIds.exists(_ == userId) }
      .toSet
  }

  def getPermission(id: AccessItemId): DomainValidation[Permission] = {
    for {
      accessItem <- getByKey(id)
      permission <- {
        accessItem match {
          case permission: Permission => permission.successNel[String]
          case _ => EntityCriteriaError(s"access item a not a permission: $id").failureNel[Permission]
        }
      }
    } yield permission
  }

  def initPermissions(): Unit = {
    log.trace("accessItemRepository:initPermissions")
    val permissions = Set[Permission](

        // FIXME: names and descriptions should be translated to different languages

        // USER PERMISSIONS
        createPermissionSimple(PermissionId.UserUpdate,
                               "Update user",
                               "User can update other users",
                               Set(RoleId.UserAdministrator)),
        createPermissionSimple(PermissionId.UserChangeState,
                               "Change user state",
                               "User can change state on other users",
                               Set(RoleId.UserAdministrator)),
        createPermissionSimple(PermissionId.UserRead,
                               "Read user",
                               "User can view information for other users",
                               Set(RoleId.UserAdministrator)),

        // ROLE PERMISSIONS
        createPermissionSimple(PermissionId.RoleRead,
                               "View roles",
                               "User can view roles",
                               Set(RoleId.UserAdministrator)),
        createPermissionSimple(PermissionId.RoleCreate,
                               "Create roles",
                               "User can create roles",
                               Set(RoleId.UserAdministrator)),
        createPermissionSimple(PermissionId.RoleUpdate,
                               "Update roles",
                               "User can update roles",
                               Set(RoleId.UserAdministrator)),
        createPermissionSimple(PermissionId.RoleDelete,
                               "Delete roles",
                               "User can remove a role",
                               Set(RoleId.UserAdministrator)),

        // MEMBERSHIP PERMISSIONS
        createPermissionSimple(PermissionId.MembershipRead,
                               "Read memberships",
                               "User can view memberships",
                               Set(RoleId.UserAdministrator)),
        createPermissionSimple(PermissionId.MembershipCreate,
                               "Create memberships",
                               "User can create memberships",
                               Set(RoleId.UserAdministrator)),
        createPermissionSimple(PermissionId.MembershipUpdate,
                               "Update memberships",
                               "User can update memberships",
                               Set(RoleId.UserAdministrator)),
        createPermissionSimple(PermissionId.MembershipDelete,
                               "Remove memberships",
                               "User can remove memberships",
                               Set(RoleId.UserAdministrator)),

        // CENTRE PERMISSIONS
        createPermissionSimple(PermissionId.CentreRead,
                               "Read centres",
                               "User can view centres",
                               Set(RoleId.CentreUser, RoleId.ShippingUser)),
        createPermissionSimple(PermissionId.CentreCreate,
                               "Create centres",
                               "User can create centres",
                               Set(RoleId.CentreAdministrator)),
        createPermissionSimple(PermissionId.CentreUpdate,
                               "Update centres",
                               "User can update centres",
                               Set(RoleId.CentreAdministrator)),
        createPermissionSimple(PermissionId.CentreChangeState,
                               "Change state on centres",
                               "User can change states on centres",
                               Set(RoleId.CentreAdministrator)),

        // STUDY PERMISSIONS
        createPermissionSimple(PermissionId.StudyRead,
                               "Read studies",
                               "User can view studies",
                               Set(RoleId.StudyUser, RoleId.SpecimenCollector, RoleId.ShippingUser)),
        createPermissionSimple(PermissionId.StudyCreate,
                               "Create studies",
                               "User can create studies",
                               Set(RoleId.StudyAdministrator)),
        createPermissionSimple(PermissionId.StudyUpdate,
                               "Update studies",
                               "User can update studies",
                               Set(RoleId.StudyAdministrator)),
        createPermissionSimple(PermissionId.StudyChangeState,
                               "Change states on studies",
                               "User can change states on studies",
                               Set(RoleId.StudyAdministrator)),

        // PARTICIPANT PERMISSIONS
        createPermissionSimple(PermissionId.ParticipantRead,
                               "Read participants",
                               "User can view participants",
                               Set(RoleId.SpecimenCollector)),
        createPermissionSimple(PermissionId.ParticipantCreate,
                               "Create participants",
                               "User can create participants",
                               Set(RoleId.SpecimenCollector)),
        createPermissionSimple(PermissionId.ParticipantUpdate,
                               "Update participants",
                               "User can update participants",
                               Set(RoleId.SpecimenCollector)),
        createPermissionSimple(PermissionId.ParticipantDelete,
                               "Delete participants",
                               "User can update participants",
                               Set(RoleId.StudyAdministrator)),

        // COLLECTION EVENT PERMISSIONS
        createPermissionSimple(PermissionId.CollectionEventRead,
                               "Read collection events",
                               "User can view collectionEvents",
                               Set(RoleId.SpecimenCollector, RoleId.ShippingUser)),
        createPermissionSimple(PermissionId.CollectionEventCreate,
                               "Create collection events",
                               "User can create collectionEvents",
                               Set(RoleId.SpecimenCollector)),
        createPermissionSimple(PermissionId.CollectionEventUpdate,
                               "Update collection events",
                               "User can update collectionEvents",
                               Set(RoleId.SpecimenCollector)),
        createPermissionSimple(PermissionId.CollectionEventDelete,
                               "Delete collection events",
                               "User can update collectionEvents",
                               Set(RoleId.SpecimenCollector)),

        // SPECIMEN PERMISSIONS
        createPermissionSimple(PermissionId.SpecimenRead,
                               "Read specimens",
                               "User can view specimens",
                               Set(RoleId.SpecimenCollector, RoleId.ShippingUser)),
        createPermissionSimple(PermissionId.SpecimenCreate,
                               "Create specimens",
                               "User can create specimens",
                               Set(RoleId.SpecimenCollector)),
        createPermissionSimple(PermissionId.SpecimenUpdate,
                               "Update specimens",
                               "User can update specimens",
                               Set(RoleId.SpecimenCollector)),
        createPermissionSimple(PermissionId.SpecimenDelete,
                               "Delete specimens",
                               "User can update specimens",
                               Set(RoleId.SpecimenCollector)),
        createPermissionSimple(PermissionId.SpecimenChangeState,
                               "Change state on specimens",
                               "User can change states on specimens",
                               Set(RoleId.StudyUser)),

        // SHIPMENT PERMISSIONS
        createPermissionSimple(PermissionId.ShipmentRead,
                               "Read shipments",
                               "User can view shipments",
                               Set(RoleId.ShippingUser)),
        createPermissionSimple(PermissionId.ShipmentCreate,
                               "Create shipments",
                               "User can create shipments",
                               Set(RoleId.ShippingUser)),
        createPermissionSimple(PermissionId.ShipmentUpdate,
                               "Update shipments",
                               "User can update shipments",
                               Set(RoleId.ShippingUser)),
        createPermissionSimple(PermissionId.ShipmentChangeState,
                               "Change state on shipments",
                               "User can change states on shipments",
                               Set(RoleId.StudyUser)),
        createPermissionSimple(PermissionId.ShipmentDelete,
                               "Delete shipments",
                               "User can delete shipments",
                               Set(RoleId.ShippingAdministrator))
      )
    permissions.foreach(p => put(p.copy(slug = slug(p.name))))
  }

  def initRoles(): Unit = {
    log.trace("accessItemRepository:initRoles")
    val roles = Set[Role](
        // FIXME: names and descriptions should be translated to different languages

        createRoleSimple(RoleId.SpecimenProcessor,
                         "Process specimens",
                         "SpecimenProcessor"),

        createRole(roleId      = RoleId.UserAdministrator,
                   name        = "User administrator",
                   description = "UserAdministrator",
                   parentIds   = Set(RoleId.WebsiteAdministrator),
                   childrenIds = Set(PermissionId.UserUpdate,
                                     PermissionId.UserChangeState,
                                     PermissionId.UserRead,
                                     PermissionId.MembershipCreate,
                                     PermissionId.MembershipUpdate,
                                     PermissionId.MembershipDelete)),

        createRole(roleId      = RoleId.CentreUser,
                   name        = "Centre User",
                   description = "Centre User",
                   parentIds   = Set(RoleId.CentreAdministrator),
                   childrenIds = Set(PermissionId.CentreRead)),

        createRole(roleId      = RoleId.CentreAdministrator,
                   name        = "Centre Administrator",
                   description = "Centre Administrator",
                   parentIds   = Set(RoleId.WebsiteAdministrator),
                   childrenIds = Set(PermissionId.CentreUpdate,
                                     PermissionId.CentreChangeState,
                                     RoleId.CentreUser,
                                     RoleId.ShippingAdministrator)),

        createRole(roleId      = RoleId.StudyUser,
                   name        = "Study User",
                   description = "Study User",
                   parentIds   = Set(RoleId.StudyAdministrator),
                   childrenIds = Set(PermissionId.StudyRead,
                                     PermissionId.SpecimenChangeState,
                                     PermissionId.SpecimenDelete,
                                     RoleId.SpecimenCollector)),

        createRole(roleId      = RoleId.StudyAdministrator,
                   name        = "Study Administrator",
                   description = "Study Administrator",
                   parentIds   = Set(RoleId.WebsiteAdministrator),
                   childrenIds = Set(PermissionId.StudyUpdate,
                                     PermissionId.StudyChangeState,
                                     RoleId.StudyUser)),

        createRole(roleId      = RoleId.SpecimenCollector,
                   name        = "Specimen Collector",
                   description = "Specimen Collector",
                   parentIds   = Set(RoleId.StudyUser),
                   childrenIds = Set(PermissionId.SpecimenRead,
                                     PermissionId.SpecimenCreate,
                                     PermissionId.SpecimenUpdate)),

        createRole(roleId      = RoleId.ShippingAdministrator,
                   name        = "Shipping Administrator",
                   description = "Shipping Administrator",
                   parentIds   = Set(RoleId.CentreAdministrator),
                   childrenIds = Set(RoleId.ShippingUser,
                                     PermissionId.ShipmentDelete)),

        createRole(roleId      = RoleId.ShippingUser,
                   name        = "Shipping User",
                   description = "Shipping User",
                   parentIds   = Set(RoleId.ShippingAdministrator),
                   childrenIds = Set(PermissionId.ShipmentRead,
                                     PermissionId.ShipmentCreate,
                                     PermissionId.ShipmentUpdate,
                                     PermissionId.ShipmentChangeState,
                                     PermissionId.CentreRead,
                                     PermissionId.SpecimenRead,
                                     PermissionId.CollectionEventRead,
                                     PermissionId.StudyRead)),

        createRole(roleId      = RoleId.WebsiteAdministrator,
                   name        = "Website Administrator",
                   description = "Website Administrator",
                   userIds     = Set(Global.DefaultUserId),
                   parentIds   = Set.empty[AccessItemId],
                   childrenIds = Set(PermissionId.StudyCreate,
                                     PermissionId.CentreCreate,
                                     RoleId.UserAdministrator,
                                     RoleId.StudyAdministrator,
                                     RoleId.CentreAdministrator,
                                     RoleId.SpecimenCollector,
                                     RoleId.SpecimenProcessor,
                                     RoleId.ShippingAdministrator))
      )


    roles.foreach(r => put(r.copy(slug = slug(r.name))))
  }

  private def createPermission(permissionId: PermissionId,
                               name:         String,
                               description:  String,
                               parentIds:    Set[AccessItemId],
                               childrenIds:  Set[AccessItemId]): Permission =
    Permission(id           = AccessItemId(permissionId.toString),
               version      = 0,
               timeAdded    = Global.StartOfTime,
               timeModified = None,
               slug         = Slug(permissionId.toString),
               name         = name,
               description  = Some(description),
               parentIds    = parentIds,
               childrenIds  = childrenIds)

  private def createPermissionSimple(permissionId: PermissionId,
                                     name:         String,
                                     description:  String,
                                     parentIds:    Set[AccessItemId]): Permission =
      createPermission(permissionId, name, description, parentIds, Set.empty[AccessItemId])


  @SuppressWarnings(Array("org.wartremover.warts.DefaultArguments"))
  private def createRole(roleId:       RoleId,
                         name:         String,
                         description:  String,
                         userIds:      Set[UserId] = Set.empty[UserId],
                         parentIds:    Set[AccessItemId],
                         childrenIds:  Set[AccessItemId]): Role =
    Role(id           = AccessItemId(roleId.toString),
         version      = 0,
         timeAdded    = Global.StartOfTime,
         timeModified = None,
         slug         = Slug(roleId.toString),
         name         = name,
         description  = Some(description),
         userIds      = userIds,
         parentIds    = parentIds,
         childrenIds  = childrenIds)

  private def createRoleSimple(roleId: RoleId, name: String, description: String): Role =
    createRole(roleId      = roleId,
               name        = name,
               description = description,
               parentIds   = Set.empty[AccessItemId],
               childrenIds = Set.empty[AccessItemId])
}
