package org.biobank.domain.access

import RoleId._
import PermissionId._
import org.biobank.Global
import org.biobank.domain._
import org.biobank.domain.user.UserId
import org.biobank.infrastructure.JsonUtils._
import org.joda.time.DateTime
import play.api.libs.json._

/**
 * Predicates that can be used to filter collections of accessItems.
 *
 */
trait AccessItemPredicates extends HasNamePredicates[AccessItem] {

  type AccessItemFilter = AccessItem => Boolean

}

class AccessItemType(val id: String) extends AnyVal {
  override def toString: String = id
}

/** Identifies a unique [[AccessItem]] in the system.
  *
  * Used as a value object to maintain associations to with other entities in the system.
  */
final case class AccessItemId(id: String) extends IdentifiedValueObject[String]

object AccessItemId {

  // Do not want JSON to create a sub object, we just want it to be converted
  // to a single string
  implicit val accessItemIdFormat: Format[AccessItemId] = new Format[AccessItemId] {

      override def writes(id: AccessItemId): JsValue = JsString(id.id)

      override def reads(json: JsValue): JsResult[AccessItemId] =
        Reads.StringReads.reads(json).map(AccessItemId.apply _)
    }

}

sealed trait AccessItem
    extends ConcurrencySafeEntity[AccessItemId]
    with HasUniqueName
    with HasOptionalDescription {

  val accessItemType: AccessItemType

  val NameMinLength: Long = 2L

  val parentIds: Set[AccessItemId]

  val childrenIds: Set[AccessItemId]

  override def toString: String =
    s"""|${this.getClass.getSimpleName}: {
        |  id:             $id,
        |  version:        $version,
        |  timeAdded:      $timeAdded,
        |  timeModified:   $timeModified,
        |  name:           $name,
        |  description:    $description,
        |  accessItemType: $accessItemType
        |  parentIds:      $parentIds,
        |  childrenIds:    $childrenIds
        |}""".stripMargin
}

object AccessItem {

  val roleAccessItemType: AccessItemType       = new AccessItemType("role")
  val permissionAccessItemType: AccessItemType = new AccessItemType("permission")

  @SuppressWarnings(Array("org.wartremover.warts.Option2Iterable"))
  implicit val accessItemFormat: Format[AccessItem] = new Format[AccessItem] {
      override def writes(accessItem: AccessItem): JsValue = {
        ConcurrencySafeEntity.toJson(accessItem) ++
        Json.obj("accessItemType" -> accessItem.accessItemType.id,
                 "name"           -> accessItem.name,
                 "parentIds"      -> accessItem.parentIds,
                 "childrenIds"    -> accessItem.childrenIds) ++
        JsObject(
          Seq[(String, JsValue)]() ++
            accessItem.description.map("description" -> Json.toJson(_)))
      }

      override def reads(json: JsValue): JsResult[AccessItem] =
        (json \ "accessItemType") match {
          case JsDefined(JsString(roleAccessItemType.id))        => json.validate[Role]
          case JsDefined(JsString(permissionAccessItemType.id))  => json.validate[Permission]
          case _ => JsError("error")
        }
    }

  @SuppressWarnings(Array("org.wartremover.warts.ImplicitConversion"))
  implicit def roleIdToAccessItemId(roleId: RoleId): AccessItemId = AccessItemId(roleId.toString)

  @SuppressWarnings(Array("org.wartremover.warts.ImplicitConversion"))
  implicit def permissionIdToAccessItemId(permissionId: PermissionId): AccessItemId =
    AccessItemId(permissionId.toString)

  @SuppressWarnings(Array("org.wartremover.warts.ImplicitConversion"))
  implicit def permissionIdsToAccessItemId(permissionIds: Set[PermissionId]): Set[AccessItemId] =
    permissionIds.map(p => AccessItemId(p.toString))

  implicit val disabledStudyFormat: Reads[Role] = Json.format[Role]
  implicit val permissionFormat: Reads[Permission] = Json.format[Permission]

  val sort2Compare: Map[String, (Role, Role) => Boolean] =
    Map[String, (Role, Role) => Boolean]("name"  -> compareByName)

  def compareByName(a: Role, b: Role): Boolean = {
    (a.name compareToIgnoreCase b.name) < 0
  }
}

final case class Role(id:           AccessItemId,
                      version:      Long,
                      timeAdded:    DateTime,
                      timeModified: Option[DateTime],
                      name:         String,
                      description:  Option[String],
                      userIds:      Set[UserId],
                      parentIds:    Set[AccessItemId],
                      childrenIds:  Set[AccessItemId])
    extends { val accessItemType: AccessItemType = AccessItem.roleAccessItemType }
    with AccessItem {
  import org.biobank.domain.CommonValidations._

  /** Used to change the name. */
  def addUser(userId: UserId): Role = {
    copy(userIds      = userIds + userId,
         version      = version + 1,
         timeModified = Some(DateTime.now))
  }

  /** Used to change the name. */
  def removeUser(userId: UserId): Role = {
    copy(userIds      = userIds - userId,
         version      = version + 1,
         timeModified = Some(DateTime.now))
  }

  /** Used to change the name. */
  def withName(name: String): DomainValidation[Role] = {
    validateString(name, NameMinLength, InvalidName) map { _ =>
      copy(name         = name,
           version      = version + 1,
           timeModified = Some(DateTime.now))
    }
  }

  /** Used to change the description. */
  def withDescription(description: Option[String]): DomainValidation[Role] = {
    validateNonEmptyOption(description, InvalidDescription) map { _ =>
      copy(description  = description,
           version      = version + 1,
           timeModified = Some(DateTime.now))
    }
  }

  def addParent(role: Role): Role = {
    copy(parentIds    = parentIds + role.id,
         version      = version + 1,
         timeModified = Some(DateTime.now))
  }

  def removeParent(role: Role): Role = {
    copy(parentIds    = parentIds - role.id,
         version      = version + 1,
         timeModified = Some(DateTime.now))
  }

  def addChild(accessItem: AccessItem): AccessItem = {
    copy(childrenIds  = childrenIds + accessItem.id,
         version      = version + 1,
         timeModified = Some(DateTime.now))
  }

  def removeChild(accessItem: AccessItem): AccessItem = {
    copy(childrenIds  = childrenIds - accessItem.id,
         version      = version + 1,
         timeModified = Some(DateTime.now))
  }


  override def toString: String =
    s"""|${this.getClass.getSimpleName}: {
        |  id:             $id,
        |  version:        $version,
        |  timeAdded:      $timeAdded,
        |  timeModified:   $timeModified,
        |  name:           $name,
        |  description:    $description,
        |  accessItemType: $accessItemType
        |  userIds:        $userIds,
        |  parentIds:      $parentIds,
        |  childrenIds:    $childrenIds
        |}""".stripMargin
}

final case class Permission(id:           AccessItemId,
                            version:      Long,
                            timeAdded:    DateTime,
                            timeModified: Option[DateTime],
                            name:         String,
                            description:  Option[String],
                            parentIds:    Set[AccessItemId],
                            childrenIds:  Set[AccessItemId])
    extends { val accessItemType: AccessItemType = AccessItem.permissionAccessItemType }
    with AccessItem {

  def addParent(accessItem: AccessItem): Permission = {
    copy(parentIds    = parentIds + accessItem.id,
         version      = version + 1,
         timeModified = Some(DateTime.now))
  }

  def removeParent(accessItem: AccessItem): Permission = {
    copy(parentIds    = parentIds - accessItem.id,
         version      = version + 1,
         timeModified = Some(DateTime.now))
  }

  def addChild(accessItem: AccessItem): Permission = {
    copy(childrenIds  = childrenIds + accessItem.id,
         version      = version + 1,
         timeModified = Some(DateTime.now))
  }

  def removeChild(accessItem: AccessItem): Permission = {
    copy(childrenIds  = childrenIds - accessItem.id,
         version      = version + 1,
         timeModified = Some(DateTime.now))
  }
}

object Permission {
  def create(id:           AccessItemId,
             name:         String,
             description:  Option[String],
             parentIds:    Set[AccessItemId],
             childrenIds:  Set[AccessItemId]): Permission =
    Permission(id           = id,
               version      = 0,
               timeAdded    = Global.StartOfTime,
               timeModified = None,
               name         = name,
               description  = description,
               parentIds    = parentIds,
               childrenIds  = childrenIds)
}
