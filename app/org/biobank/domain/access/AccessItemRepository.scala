package org.biobank.domain.access

import com.google.inject.ImplementedBy
import javax.inject.Singleton
import org.biobank.Global
import org.biobank.domain._
import org.biobank.domain.access.PermissionId._
import org.biobank.domain.access.RoleId._
import org.biobank.domain.user.UserId
import org.slf4j.{Logger, LoggerFactory}
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

@ImplementedBy(classOf[AccessItemRepositoryImpl])
trait AccessItemRepository extends ReadWriteRepository[AccessItemId, AccessItem] {

  def getRole(id: AccessItemId): DomainValidation[Role]

  def getRoles(): Set[Role]

  def rolesForUser(userId: UserId): Set[Role]

  def getPermission(id: AccessItemId): DomainValidation[Permission]

}

@Singleton
class AccessItemRepositoryImpl extends ReadWriteRepositoryRefImpl[AccessItemId, AccessItem](v => v.id)
    with AccessItemRepository {

  import org.biobank.CommonValidations._
  import org.biobank.domain.access.AccessItem._

  val log: Logger = LoggerFactory.getLogger(this.getClass)

  def nextIdentity: AccessItemId = new AccessItemId(nextIdentityAsString)

  def accessItemNotFound(id: AccessItemId): IdNotFound = IdNotFound(s"accessItem id: $id")

  override def getByKey(id: AccessItemId): DomainValidation[AccessItem] = {
    getMap.get(id).toSuccessNel(accessItemNotFound(id).toString)
  }

  def getRole(id: AccessItemId): DomainValidation[Role] = {
    for {
      accessItem <- getByKey(id)
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

    def roleChildren(role: Role): Set[Role] = {
      role.childrenIds.map(getByKey).toList.sequenceU.fold(
        err => Set.empty[Role],
        list => list.flatMap { item =>
          item match {
            case r: Role => roleChildren(r) + role
            case r => Set(role)
          }
        }.toSet
      )
    }

    val hasRole = getValues.find { item =>
        item match {
          case r: Role => r.userIds.exists(_ == userId)
          case _ => false
        }
      }

    hasRole match {
      case Some(role: Role) => roleChildren(role) + role
      case _ => Set.empty[Role]
    }
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

        // USER PERMISSIONS
        createPermissionSimple(PermissionId.UserUpdate,
                               "User can update other users",
                               Set(RoleId.UserAdministrator)),
        createPermissionSimple(PermissionId.UserChangeState,
                               "User can change state on other users",
                               Set(RoleId.UserAdministrator)),
        createPermissionSimple(PermissionId.UserRead,
                               "User can view information for other users",
                               Set(RoleId.UserAdministrator)),

        // CENTRE PERMISSIONS
        createPermissionSimple(PermissionId.CentreRead,
                               "User can view centres",
                               Set(RoleId.CentreUser)),
        createPermissionSimple(PermissionId.CentreCreate,
                               "User can create centres",
                               Set(RoleId.CentreAdministrator)),
        createPermissionSimple(PermissionId.CentreUpdate,
                               "User can update centres",
                               Set(RoleId.CentreAdministrator)),
        createPermissionSimple(PermissionId.CentreChangeState,
                               "User can change states on centres",
                               Set(RoleId.CentreAdministrator)),

        // STUDY PERMISSIONS
        createPermissionSimple(PermissionId.StudyRead,
                               "User can view studies",
                               Set(RoleId.StudyUser,
                                   RoleId.SpecimenCollector)),
        createPermissionSimple(PermissionId.StudyCreate,
                               "User can create studies",
                               Set(RoleId.StudyAdministrator)),
        createPermissionSimple(PermissionId.StudyUpdate,
                               "User can update studies",
                               Set(RoleId.StudyAdministrator)),
        createPermissionSimple(PermissionId.StudyChangeState,
                               "User can change states on studies",
                               Set(RoleId.StudyAdministrator)),

        // PARTICIPANT PERMISSIONS
        createPermissionSimple(PermissionId.ParticipantRead,
                               "User can view participants",
                               Set(RoleId.SpecimenCollector)),
        createPermissionSimple(PermissionId.ParticipantCreate,
                               "User can create participants",
                               Set(RoleId.SpecimenCollector)),
        createPermissionSimple(PermissionId.ParticipantUpdate,
                               "User can update participants",
                               Set(RoleId.SpecimenCollector)),
        createPermissionSimple(PermissionId.ParticipantDelete,
                               "User can update participants",
                               Set(RoleId.StudyAdministrator)),

        // COLLECTION EVENT PERMISSIONS
        createPermissionSimple(PermissionId.CollectionEventRead,
                               "User can view collectionEvents",
                               Set(RoleId.SpecimenCollector)),
        createPermissionSimple(PermissionId.CollectionEventCreate,
                               "User can create collectionEvents",
                               Set(RoleId.SpecimenCollector)),
        createPermissionSimple(PermissionId.CollectionEventUpdate,
                               "User can update collectionEvents",
                               Set(RoleId.SpecimenCollector)),
        createPermissionSimple(PermissionId.CollectionEventDelete,
                               "User can update collectionEvents",
                               Set(RoleId.SpecimenCollector)),

        // SPECIMEN PERMISSIONS
        createPermissionSimple(PermissionId.SpecimenRead,
                               "User can view specimens",
                               Set(RoleId.SpecimenCollector)),
        createPermissionSimple(PermissionId.SpecimenCreate,
                               "User can create specimens",
                               Set(RoleId.SpecimenCollector)),
        createPermissionSimple(PermissionId.SpecimenUpdate,
                               "User can update specimens",
                               Set(RoleId.SpecimenCollector)),
        createPermissionSimple(PermissionId.SpecimenDelete,
                               "User can update specimens",
                               Set(RoleId.SpecimenCollector)),
        createPermissionSimple(PermissionId.SpecimenChangeState,
                               "User can change states on specimens",
                               Set(RoleId.StudyUser))
      )
    permissions.foreach(put)
  }

  private def createPermission(permissionId: PermissionId,
                               description:  String,
                               parentIds:    Set[AccessItemId],
                               childrenIds:  Set[AccessItemId]): Permission =
    Permission(id           = AccessItemId(permissionId.toString),
               version      = 0,
               timeAdded    = Global.StartOfTime,
               timeModified = None,
               name         = permissionId.toString,
               description  = Some(description),
               parentIds    = parentIds,
               childrenIds  = childrenIds)

  private def createPermissionSimple(permissionId: PermissionId,
                                     description:  String,
                                     parentIds:    Set[AccessItemId]): Permission =
      createPermission(permissionId, description, parentIds, Set.empty[AccessItemId])

  def initRoles(): Unit = {
    log.trace("accessItemRepository:initRoles")
    val roles = Set[Role](
        createRoleSimple(RoleId.SpecimenProcessor,     "SpecimenProcessor"),

        createRole(roleId      = RoleId.UserAdministrator,
                   description = "UserAdministrator",
                   parentIds   = Set(RoleId.WebsiteAdministrator),
                   childrenIds = Set(PermissionId.UserUpdate,
                                     PermissionId.UserChangeState,
                                     PermissionId.UserRead)),

        createRole(roleId      = RoleId.CentreUser,
                   description = "Centre User",
                   parentIds   = Set(RoleId.CentreAdministrator),
                   childrenIds = Set(PermissionId.CentreRead)),

        createRole(roleId      = RoleId.CentreAdministrator,
                   description = "Centre Administrator",
                   parentIds   = Set(RoleId.WebsiteAdministrator),
                   childrenIds = Set(PermissionId.CentreCreate,
                                     PermissionId.CentreUpdate,
                                     PermissionId.CentreChangeState,
                                     RoleId.CentreUser)),

        createRole(roleId      = RoleId.StudyUser,
                   description = "Study User",
                   parentIds   = Set(RoleId.StudyAdministrator),
                   childrenIds = Set(PermissionId.StudyRead,
                                     PermissionId.SpecimenChangeState)),

        createRole(roleId      = RoleId.StudyAdministrator,
                   description = "Study Administrator",
                   parentIds   = Set(RoleId.WebsiteAdministrator),
                   childrenIds = Set(PermissionId.StudyCreate,
                                     PermissionId.StudyUpdate,
                                     PermissionId.StudyChangeState,
                                     RoleId.StudyUser)),

        createRole(roleId      = RoleId.SpecimenCollector,
                   description = "SpecimenCollector",
                   parentIds   = Set(RoleId.StudyUser),
                   childrenIds = Set(PermissionId.SpecimenRead,
                                     PermissionId.SpecimenCreate,
                                     PermissionId.SpecimenUpdate,
                                     PermissionId.SpecimenDelete)),

        createRole(roleId = RoleId.ShippingAdministrator,
                   description = "ShippingAdministrator",
                   parentIds   = Set(RoleId.CentreAdministrator),
                   childrenIds = Set(RoleId.ShippingUser,
                                     PermissionId.ShipmentDelete)),

        createRole(roleId = RoleId.ShippingUser,
                   description = "ShippingAdministrator",
                   parentIds   = Set(RoleId.ShippingAdministrator),
                   childrenIds = Set(PermissionId.ShipmentRead,
                                     PermissionId.ShipmentCreate,
                                     PermissionId.ShipmentUpdate,
                                     PermissionId.ShipmentChangeState)),

        createRole(roleId      = RoleId.WebsiteAdministrator,
                   description = "WebsiteAdministrator",
                   userIds     = Set(Global.DefaultUserId),
                   parentIds   = Set.empty[AccessItemId],
                   childrenIds = Set(RoleId.UserAdministrator,
                                     RoleId.StudyAdministrator,
                                     RoleId.CentreAdministrator,
                                     RoleId.SpecimenCollector,
                                     RoleId.SpecimenProcessor))
      )


    roles.foreach(put)
  }

  @SuppressWarnings(Array("org.wartremover.warts.DefaultArguments"))
  private def createRole(roleId:       RoleId,
                         description:  String,
                         userIds:      Set[UserId] = Set.empty[UserId],
                         parentIds:    Set[AccessItemId],
                         childrenIds:  Set[AccessItemId]): Role =
    Role(id           = AccessItemId(roleId.toString),
         version      = 0,
         timeAdded    = Global.StartOfTime,
         timeModified = None,
         name         = roleId.toString,
         description  = Some(description),
         userIds      = userIds,
         parentIds    = parentIds,
         childrenIds  = childrenIds)

  private def createRoleSimple(roleId: RoleId, description: String): Role =
    createRole(roleId      = roleId,
               description = description,
               parentIds   = Set.empty[AccessItemId],
               childrenIds = Set.empty[AccessItemId])

  private def init(): Unit = {
    initPermissions
    initRoles
  }

  init
}
