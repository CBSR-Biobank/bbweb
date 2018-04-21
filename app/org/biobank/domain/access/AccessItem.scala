package org.biobank.domain.access

import java.time.OffsetDateTime
import RoleId._
import PermissionId._
import org.biobank.Global
import org.biobank.domain._
import org.biobank.domain.users.UserId
import play.api.libs.json._
import scalaz.Scalaz._

/**
 * Predicates that can be used to filter collections of accessItems.
 *
 */
trait AccessItemPredicates[T <: AccessItem] extends HasNamePredicates[T] {

  type AccessItemFilter = T => Boolean

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

trait AccessItemValidations {

  val NameMinLength: Long = 2L

}

sealed trait AccessItem
    extends ConcurrencySafeEntity[AccessItemId]
    with HasUniqueName
    with HasSlug
    with HasOptionalDescription {

  import org.biobank.domain.DomainValidations._

  val accessItemType: AccessItemType

  val NameMinLength: Long = 2L

  val parentIds: Set[AccessItemId]

  val childrenIds: Set[AccessItemId]

  protected def parentIdNotSelf(parentId: AccessItemId): DomainValidation[AccessItem] = {
    if (this.id == parentId) {
      DomainError(s"parent ID cannot be self").failureNel[AccessItem]
    } else {
      this.successNel[String]
    }
  }

  protected def notAlreadyParent(parentId: AccessItemId): DomainValidation[AccessItem] = {
    if (this.parentIds.exists(_ == parentId)) {
      DomainError(s"parent ID is already in role: ${parentId}").failureNel[AccessItem]
    } else {
      this.successNel[String]
    }
  }

  protected def isParent(parentId: AccessItemId): DomainValidation[AccessItem] = {
    if (this.parentIds.exists(_ == parentId)) {
      this.successNel[String]
    } else {
      DomainError(s"parent ID not in role: ${parentId}").failureNel[AccessItem]
    }
  }

  protected def childIdNotSelf(childId: AccessItemId): DomainValidation[AccessItem] = {
    if (this.id == childId) {
      DomainError(s"child ID cannot be self").failureNel[AccessItem]
    } else {
      this.successNel[String]
    }
  }

  protected def notAlreadyChild(childId: AccessItemId): DomainValidation[AccessItem] = {
    if (this.childrenIds.exists(_ == childId)) {
      DomainError(s"child ID is already in role: ${childId}").failureNel[AccessItem]
    } else {
      this.successNel[String]
    }
  }

  protected def isChild(childId: AccessItemId): DomainValidation[AccessItem] = {
    if (this.childrenIds.exists(_ == childId)) {
      this.successNel[String]
    } else {
      DomainError(s"child ID not in role: ${childId}").failureNel[AccessItem]
    }
  }

  def addParent(parentId: AccessItemId): DomainValidation[AccessItem] = {
    (validateId(parentId, InvalidAccessItemId) |@|
       parentIdNotSelf(parentId) |@|
       notAlreadyParent(parentId)) { case _ =>
        this
    }
  }

  def removeParent(parentId: AccessItemId): DomainValidation[AccessItem] = {
    (validateId(parentId, InvalidAccessItemId) |@|
       isParent(parentId)) { case _ =>
        this
    }
  }

  def addChild(childId: AccessItemId): DomainValidation[AccessItem] = {
    (validateId(childId, InvalidAccessItemId) |@|
       childIdNotSelf(childId) |@|
       notAlreadyChild(childId)) { case _ =>
        this
    }
  }

  def removeChild(childId: AccessItemId): DomainValidation[AccessItem] = {
    (validateId(childId, InvalidAccessItemId) |@|
       isChild(childId)) { case _ =>
        this
    }
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
        val json = ConcurrencySafeEntity.toJson(accessItem) ++
        Json.obj("accessItemType" -> accessItem.accessItemType.id,
                 "slug"           -> accessItem.slug,
                 "name"           -> accessItem.name,
                 "parentIds"      -> accessItem.parentIds,
                 "childrenIds"    -> accessItem.childrenIds) ++
        JsObject(
          Seq[(String, JsValue)]() ++
            accessItem.description.map("description" -> Json.toJson(_)))

        accessItem match {
          case role: Role =>
            json ++ Json.obj("userIds" -> role.userIds)
          case _ =>
            json
        }
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

  val sort2Compare: Map[String, (AccessItem, AccessItem) => Boolean] =
    Map[String, (AccessItem, AccessItem) => Boolean]("name"  -> compareByName)

  def compareByName(a: AccessItem, b: AccessItem): Boolean = {
    (a.name compareToIgnoreCase b.name) < 0
  }
}

final case class Role(id:           AccessItemId,
                      version:      Long,
                      timeAdded:    OffsetDateTime,
                      timeModified: Option[OffsetDateTime],
                      slug:         Slug,
                      name:         String,
                      description:  Option[String],
                      userIds:      Set[UserId],
                      parentIds:    Set[AccessItemId],
                      childrenIds:  Set[AccessItemId])
    extends { val accessItemType: AccessItemType = AccessItem.roleAccessItemType }
    with AccessItem {
  import org.biobank.CommonValidations._
  import org.biobank.domain.DomainValidations._

  /** Used to change the name. */
  def addUser(userId: UserId): DomainValidation[Role] = {
    validateId(userId, InvalidUserId).map { _ =>
      copy(userIds      = userIds + userId,
           version      = version + 1,
           timeModified = Some(OffsetDateTime.now))
    }
  }

  /** Used to change the name. */
  def removeUser(userId: UserId): DomainValidation[Role] = {
    validateId(userId, InvalidUserId).map { _ =>
      copy(userIds      = userIds - userId,
           version      = version + 1,
           timeModified = Some(OffsetDateTime.now))
    }
  }

  /** Used to change the name. */
  def withName(name: String): DomainValidation[Role] = {
    validateString(name, NameMinLength, InvalidName) map { _ =>
      copy(name         = name,
           version      = version + 1,
           timeModified = Some(OffsetDateTime.now))
    }
  }

  /** Used to change the description. */
  def withDescription(description: Option[String]): DomainValidation[Role] = {
    validateNonEmptyStringOption(description, InvalidDescription) map { _ =>
      copy(description  = description,
           version      = version + 1,
           timeModified = Some(OffsetDateTime.now))
    }
  }

  override def addParent(parentId: AccessItemId): DomainValidation[Role] = {
    super.addParent(parentId).map { _ =>
        copy(parentIds    = parentIds + parentId,
             version      = version + 1,
             timeModified = Some(OffsetDateTime.now))
    }
  }

  override def removeParent(parentId: AccessItemId): DomainValidation[Role] = {
    super.removeParent(parentId).map { _ =>
      copy(parentIds    = parentIds - parentId,
           version      = version + 1,
           timeModified = Some(OffsetDateTime.now))
    }
  }

  override def addChild(childId: AccessItemId): DomainValidation[Role] = {
    super.addChild(childId).map { _ =>
      copy(childrenIds  = childrenIds + childId,
           version      = version + 1,
           timeModified = Some(OffsetDateTime.now))
    }
  }

  override def removeChild(childId: AccessItemId): DomainValidation[Role] = {
    super.removeChild(childId).map { _ =>
      copy(childrenIds  = childrenIds - childId,
           version      = version + 1,
           timeModified = Some(OffsetDateTime.now))
    }
  }

  override def toString: String =
    s"""|${this.getClass.getSimpleName}: {
        |  id:             $id,
        |  version:        $version,
        |  timeAdded:      $timeAdded,
        |  timeModified:   $timeModified,
        |  slug:           $slug,
        |  name:           $name,
        |  description:    $description,
        |  accessItemType: $accessItemType
        |  userIds:        $userIds,
        |  parentIds:      $parentIds,
        |  childrenIds:    $childrenIds
        |}""".stripMargin
}

object Role extends AccessItemValidations {
  import org.biobank.CommonValidations._
  import org.biobank.domain.DomainValidations._

  def create(id:           AccessItemId,
             version:      Long,
             timeAdded:    OffsetDateTime,
             timeModified: Option[OffsetDateTime],
             name:         String,
             description:  Option[String],
             userIds:      Set[UserId],
             parentIds:    Set[AccessItemId],
             childrenIds:  Set[AccessItemId]): DomainValidation[Role] = {
    (validateId(id, InvalidAccessItemId) |@|
       validateString(name, NameMinLength, InvalidName) |@|
       validateNonEmptyStringOption(description, InvalidDescription) |@|
       userIds.map(validateId(_, InvalidUserId)).toList.sequenceU |@|
       parentIds.map(validateId(_, InvalidAccessItemId)).toList.sequenceU |@|
       childrenIds.map(validateId(_, InvalidAccessItemId)).toList.sequenceU) { case _ =>
        Role(id           = id,
             version      = version,
             timeAdded    = timeAdded,
             timeModified = timeModified,
             slug         = Slug(name),
             name         = name,
             description  = description,
             userIds      = userIds,
             parentIds    = parentIds,
             childrenIds  = childrenIds)
    }
  }
}

final case class Permission(id:           AccessItemId,
                            version:      Long,
                            timeAdded:    OffsetDateTime,
                            timeModified: Option[OffsetDateTime],
                            slug: Slug,
                            name:         String,
                            description:  Option[String],
                            parentIds:    Set[AccessItemId],
                            childrenIds:  Set[AccessItemId])
    extends { val accessItemType: AccessItemType = AccessItem.permissionAccessItemType }
    with AccessItem {

  override def addParent(parentId: AccessItemId): DomainValidation[Permission] = {
    super.addParent(parentId).map { case _ =>
      copy(parentIds    = parentIds + parentId,
           version      = version + 1,
           timeModified = Some(OffsetDateTime.now))
    }
  }

  override def removeParent(parentId: AccessItemId): DomainValidation[Permission] = {
    super.removeParent(parentId).map { _ =>
      copy(parentIds    = parentIds - parentId,
           version      = version + 1,
           timeModified = Some(OffsetDateTime.now))
    }
  }

  override def addChild(childId: AccessItemId): DomainValidation[Permission] = {
    super.addChild(childId).map { _ =>
      copy(childrenIds  = childrenIds + childId,
           version      = version + 1,
           timeModified = Some(OffsetDateTime.now))
    }
  }

  override def removeChild(childId: AccessItemId): DomainValidation[Permission] = {
    super.removeChild(childId).map { _ =>
      copy(childrenIds  = childrenIds - childId,
           version      = version + 1,
           timeModified = Some(OffsetDateTime.now))
    }
  }
}

object Permission extends AccessItemValidations {
  import org.biobank.CommonValidations._
  import org.biobank.domain.DomainValidations._

  def create(id:           AccessItemId,
             name:         String,
             description:  Option[String],
             parentIds:    Set[AccessItemId],
             childrenIds:  Set[AccessItemId]): DomainValidation[Permission] =
    (validateId(id, InvalidAccessItemId) |@|
       validateString(name, NameMinLength, InvalidName) |@|
       validateNonEmptyStringOption(description, InvalidDescription) |@|
       parentIds.map(validateId(_, InvalidAccessItemId)).toList.sequenceU |@|
       childrenIds.map(validateId(_, InvalidAccessItemId)).toList.sequenceU) { case _ =>
        Permission(id           = id,
                   version      = 0,
                   timeAdded    = Global.StartOfTime,
                   timeModified = None,
                   slug         = Slug(name),
                   name         = name,
                   description  = description,
                   parentIds    = parentIds,
                   childrenIds  = childrenIds)
    }
}
