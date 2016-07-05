package org.biobank.domain.containers

import org.biobank.ValidationKey
import org.biobank.domain._
import org.joda.time.DateTime

import play.api.libs.json._

trait ContainerValidations {
  val NameMinLength = 2

  case object ContainerSchemaIdInvalid extends ValidationKey

}

/**
 * A specifically built physical unit that can hold child containers, or can be contained in a parent
 * container.
 */
trait Container[T <: ContainerType]
    extends ConcurrencySafeEntity[ContainerId] {

 /**
  * An inventory identifier, such as a barcode. Global uniqueness is required so that
  * [[Container]]s, like [[Specimen]]s, can be shipped between [[Center]]s.
  */
  val inventoryId: String

  /** The ID of the container type that classifiies this [[Container]]. */
  val containerTypeId: ContainerTypeId

 /** The ID of the [[Container]] that this container is stored in. */
  val parentId: ContainerId

  /**
   * The position this [[Container]] has in its parent, or null if there is no specific
   * position. This value is always null if the parent is null.
   */
   val position: ContainerSchemaPositionId
}

@SuppressWarnings(Array("org.wartremover.warts.Any", "org.wartremover.warts.Nothing"))
object Container {

  implicit val containerWrites: Writes[Container[_]] = new Writes[Container[_]] {
    def writes(container: Container[_]) = Json.obj(
      "id"              -> container.id,
      "containerTypeId" -> container.containerTypeId,
      "parentId"        -> container.parentId,
      "version"         -> container.version,
      "timeAdded"       -> container.timeAdded,
      "timeModified"    -> container.timeModified
    )
  }

}

final case class StorageContainer(id:              ContainerId,
                                  version:         Long,
                                  timeAdded:       DateTime,
                                  timeModified:    Option[DateTime],
                                  inventoryId:     String,
                                  containerTypeId: ContainerTypeId,
                                  parentId:        ContainerId,
                                  position:        ContainerSchemaPositionId)
    extends Container[StorageContainerType]

final case class SpecimenContainer(id:              ContainerId,
                                   version:         Long,
                                   timeAdded:       DateTime,
                                   timeModified:    Option[DateTime],
                                   inventoryId:     String,
                                   containerTypeId: ContainerTypeId,
                                   parentId:        ContainerId,
                                   position:        ContainerSchemaPositionId)
    extends Container[SpecimenContainerType]
