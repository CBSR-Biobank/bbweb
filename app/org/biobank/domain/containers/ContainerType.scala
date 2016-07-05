package org.biobank.domain.containers

import org.biobank.ValidationKey
import org.biobank.domain._
import org.biobank.domain.centre.CentreId

import org.joda.time.DateTime
import play.api.libs.json._
import scalaz.Scalaz._

trait ContainerTypeValidations {
  val NameMinLength = 2

  case object ContainerSchemaIdInvalid extends ValidationKey

}

/**
 * Describes a container configuration which may hold child containers or specimens. Container types are used
 * to create a representation of a physical container
 */
sealed trait ContainerType
    extends ConcurrencySafeEntity[ContainerTypeId]
    with HasUniqueName
    with HasDescriptionOption
    with ContainerValidations {
  import org.biobank.domain.CommonValidations._

  /**
   * The [[centre.Centre]] that owns and is allowed to modify this [[ContainerType]].
   *
   * When equal to [[None]] then it is a globally accessible [[ContainerType]].
   */
  val centreId: Option[CentreId]

  /**
   * How [[Containers]] of this [[ContainerType]] are designed and laid out, with labelled positions for
   * children.
   */
  val schemaId: ContainerSchemaId

  /**
   * True if this [[ContainerType]] can be used by (but not modified) by other [[Centre]]s, otherwise false.
   */
  val shared: Boolean

  /**
   * True if this [[ContainerType]] can be used to create new [[Container]]s, or false if this
   * [[ContainerType]] is to be used only for existing [[Container]]s.
   */
  val enabled: Boolean

  def withName(name: String): DomainValidation[String] = {
    validateString(name, NameMinLength, InvalidName)
  }

  def withDescription(description:  Option[String]): DomainValidation[Option[String]] = {
    validateNonEmptyOption(description, InvalidDescription)
  }

  // def withShared(shared: Boolean): DomainValidation[StorageContainerType]

  // def withEnabled(enabled: Boolean): DomainValidation[StorageContainerType]

  override def toString: String =
    s"""|ContainerType:{
        |  id:          $id,
        |  centreId:    $centreId,
        |  schemaId:    $schemaId,
        |  name:        $name,
        |  description: $description,
        |  shared:      $shared
        |}""".stripMargin
}

object ContainerType {

  implicit val containerTypeWrites: Writes[ContainerType] = new Writes[ContainerType] {
    def writes(containerType: ContainerType) = Json.obj(
      "id"           -> containerType.id,
      "centreId"     -> containerType.centreId,
      "schemaId"     -> containerType.schemaId,
      "version"      -> containerType.version,
      "timeAdded"    -> containerType.timeAdded,
      "timeModified" -> containerType.timeModified,
      "name"         -> containerType.name,
      "description"  -> containerType.description,
      "shared"       -> containerType.shared,
      "status"       -> containerType.getClass.getSimpleName
    )
  }

}

/**
 * When a container type is enabled, it ''can'' be used to create new containers.
 */
final case class StorageContainerType(id:           ContainerTypeId,
                                      centreId:     Option[CentreId],
                                      schemaId:     ContainerSchemaId,
                                      version:      Long,
                                      timeAdded:    DateTime,
                                      timeModified: Option[DateTime],
                                      name:         String,
                                      description:  Option[String],
                                      shared:       Boolean,
                                      enabled:      Boolean)
    extends ContainerType {

  // override def withName(name: String): DomainValidation[StorageContainerType] = {
  //   super.withName(name) map { _ => copy(version = version + 1, name = name) }
  // }

  // override def withDescription(description:  Option[String]): DomainValidation[StorageContainerType] = {
  //   super.withDescription(description) map { _ =>
  //     copy(version = version + 1, description = description)
  //   }
  // }

  // override def withShared(shared: Boolean): DomainValidation[StorageContainerType] = {
  //   copy(version = version + 1, shared = shared).success
  // }

  // def withEnabled(enabled: Boolean): DomainValidation[StorageContainerType] = {
  //   copy(version = version + 1, enabled = enabled).success
  // }

}

object StorageContainerType extends ContainerValidations {
  import org.biobank.domain.CommonValidations._

  def create(id:          ContainerTypeId,
             centreId:    Option[CentreId],
             schemaId:    ContainerSchemaId,
             version:     Long,
             name:        String,
             description: Option[String],
             shared:      Boolean,
             enabled:     Boolean) = {
    (validateId(id) |@|
      validateId(centreId, CentreIdRequired) |@|
      validateId(schemaId, ContainerSchemaIdInvalid) |@|
      validateVersion(version) |@|
      validateString(name, NameMinLength, InvalidName) |@|
       validateNonEmptyOption(description, InvalidDescription)) {
      case (_, _, _, _, _, _) => StorageContainerType(id,
                                                      centreId,
                                                      schemaId,
                                                      version,
                                                      DateTime.now,
                                                      None,
                                                      name,
                                                      description,
                                                      shared,
                                                      enabled)
    }
  }

}

/**
 * When a container type is disabled, it ''can not'' be used to create new containers.
 */
final case class SpecimenContainerType(id:           ContainerTypeId,
                                       centreId:     Option[CentreId],
                                       schemaId:     ContainerSchemaId,
                                       version:      Long,
                                       timeAdded:    DateTime,
                                       timeModified: Option[DateTime],
                                       name:         String,
                                       description:  Option[String],
                                       shared:       Boolean,
                                       enabled:      Boolean)
    extends ContainerType

object SpecimenContainerType extends ContainerValidations {
  import org.biobank.domain.CommonValidations._

  def create(id:           ContainerTypeId,
             centreId:     Option[CentreId],
             schemaId:     ContainerSchemaId,
             version:      Long,
             timeAdded:    DateTime,
             timeModified: Option[DateTime],
             name:         String,
             description:  Option[String],
             shared:       Boolean,
             enabled:      Boolean): DomainValidation[SpecimenContainerType] = {
    (validateId(id) |@|
       validateId(centreId, CentreIdRequired) |@|
       validateId(schemaId, ContainerSchemaIdInvalid) |@|
       validateVersion(version) |@|
       validateString(name, NameMinLength, InvalidName) |@|
       validateNonEmptyOption(description, InvalidDescription)) {
      case (_, _, _, _, _, _) => SpecimenContainerType(id,
                                                       centreId,
                                                       schemaId,
                                                       version,
                                                       DateTime.now,
                                                       None,
                                                       name,
                                                       description,
                                                       shared,
                                                       enabled)
    }
  }
}
