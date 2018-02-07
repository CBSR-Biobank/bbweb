package org.biobank.domain.containers

import java.time.OffsetDateTime
import org.biobank.domain._
import play.api.libs.json._
import scalaz.Scalaz._

trait ContainerSchemaValidations {

  val NameMinLength: Long = 2L

}


/**
 * A plan for how the children in a {@link Container} are positioned and labelled.
 */
final case class ContainerSchema(id:           ContainerSchemaId,
                                 version:      Long,
                                 timeAdded:    OffsetDateTime,
                                 timeModified: Option[OffsetDateTime],
                                 slug:         String,
                                 name:         String,
                                 description:  Option[String],
                                 shared:       Boolean)
    extends ConcurrencySafeEntity[ContainerSchemaId]
    with HasUniqueName
    with HasOptionalDescription
    with ContainerSchemaValidations {
  import org.biobank.CommonValidations._
  import org.biobank.domain.DomainValidations._

  /** Used to change the name. */
  def withName(name: String): DomainValidation[ContainerSchema] = {
    validateString(name, NameMinLength, InvalidName) map (_ =>
      copy(version = version + 1, name = name)
    )
  }

  /** Used to change the description. */
  def withDescription(description: Option[String]): DomainValidation[ContainerSchema] = {
    validateNonEmptyStringOption(description, InvalidDescription) map { _ =>
      copy(version = version + 1, description  = description)
    }
  }

  def withShared(shared: Boolean): DomainValidation[ContainerSchema] = {
    copy(version = version + 1, shared  = shared).successNel[String]
  }

}

/**
  * Factory object used to create a container schema.
  */
object ContainerSchema extends ContainerSchemaValidations {
  import org.biobank.CommonValidations._
  import org.biobank.domain.DomainValidations._

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
       validateNonEmptyStringOption(description, InvalidDescription)) { case _ =>
        ContainerSchema(id           = id,
                        version      = version,
                        timeAdded    = OffsetDateTime.now,
                        timeModified = None,
                        slug         = Slug(name),
                        name         = name,
                        description  = description,
                        shared       = shared)
    }
  }

  implicit val containerSchemaWrites: Writes[ContainerSchema] = Json.writes[ContainerSchema]
}
