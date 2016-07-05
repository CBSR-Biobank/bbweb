package org.biobank.domain.containers

import org.biobank.domain._
import org.biobank.infrastructure.JsonUtils._

import play.api.libs.json._
import org.joda.time.DateTime
import scalaz.Scalaz._

trait ContainerSchemaValidations {

  val NameMinLength = 2

}


/**
 * A plan for how the children in a {@link Container} are positioned and labelled.
 */
final case class ContainerSchema(id:           ContainerSchemaId,
                                 version:      Long,
                                 timeAdded:    DateTime,
                                 timeModified: Option[DateTime],
                                 name:         String,
                                 description:  Option[String],
                                 shared:       Boolean)
    extends ConcurrencySafeEntity[ContainerSchemaId]
    with HasUniqueName
    with HasDescriptionOption
    with ContainerSchemaValidations {
  import CommonValidations._

  /** Used to change the name. */
  def withName(name: String): DomainValidation[ContainerSchema] = {
    validateString(name, NameMinLength, InvalidName) map (_ =>
      copy(version = version + 1, name = name)
    )
  }

  /** Used to change the description. */
  def withDescription(description: Option[String]): DomainValidation[ContainerSchema] = {
    validateNonEmptyOption(description, InvalidDescription) map (_ =>
      copy(version = version + 1, description  = description)
    )
  }

  def withShared(shared: Boolean): DomainValidation[ContainerSchema] = {
    copy(version = version + 1, shared  = shared).successNel[String]
  }

}

/**
  * Factory object used to create a container schema.
  */
object ContainerSchema extends ContainerSchemaValidations {
  import CommonValidations._

  /**
    * The factory method to create a container schema.
    *
    * Performs validation on fields.
    */
  def create(id:          ContainerSchemaId,
             version:     Long,
             name:        String,
             description: Option[String],
             shared:      Boolean)
      : DomainValidation[ContainerSchema] = {
    (validateId(id) |@|
       validateVersion(version) |@|
       validateString(name, NameMinLength, InvalidName) |@|
       validateNonEmptyOption(description, InvalidDescription)) {
      case (_, _, _, _) => ContainerSchema(id, version, DateTime.now, None, name, description, shared)
    }
  }

  implicit val containerSchemaWrites: Writes[ContainerSchema] = Json.writes[ContainerSchema]
}
