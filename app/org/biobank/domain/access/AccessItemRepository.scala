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

        // STUDY PERMISSIONS
        createPermissionSimple(PermissionId.StudyCreate,
                               "User can create studies",
                               Set(RoleId.StudyAdministrator)),
        createPermissionSimple(PermissionId.StudyRead,
                               "User can view studies",
                               Set(RoleId.StudyAdministrator)),
        createPermissionSimple(PermissionId.StudyUpdate,
                               "User can update studies",
                               Set(RoleId.StudyAdministrator)),
        createPermissionSimple(PermissionId.StudyChangeState,
                               "User can change states on studies",
                               Set(RoleId.StudyAdministrator))
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
        createRoleSimple(RoleId.ShippingAdministrator, "ShippingAdministrator"),
        createRoleSimple(RoleId.SpecimenProcessor,     "SpecimenProcessor"),
        createRoleSimple(RoleId.SpecimenCollector,     "SpecimenCollector"),
        createRoleSimple(RoleId.CentreAdministrator,   "CentreAdministrator"),

        createRole(roleId      = RoleId.StudyAdministrator,
                   description = "Study Administrator",
                   parentIds   = Set(RoleId.WebsiteAdministrator),
                   childrenIds = Set(PermissionId.StudyUpdate,
                                     PermissionId.StudyChangeState,
                                     PermissionId.StudyRead)),

        createRole(roleId      = RoleId.UserAdministrator,
                   description = "UserAdministrator",
                   parentIds   = Set(RoleId.WebsiteAdministrator),
                   childrenIds = Set(PermissionId.UserUpdate,
                                     PermissionId.UserChangeState,
                                     PermissionId.UserRead)),

        createRole(roleId      = RoleId.WebsiteAdministrator,
                   description = "WebsiteAdministrator",
                   userIds     = Set(Global.DefaultUserId),
                   parentIds   = Set.empty[AccessItemId],
                   childrenIds = Set(RoleId.UserAdministrator,
                                     RoleId.StudyAdministrator,
                                     RoleId.CentreAdministrator,
                                     RoleId.ShippingAdministrator,
                                     RoleId.SpecimenCollector,
                                     RoleId.SpecimenProcessor)))


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
