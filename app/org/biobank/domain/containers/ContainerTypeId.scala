package org.biobank.domain.containers

import org.biobank.domain._

import play.api.libs.json._

/** Identifies a unique [[ContainerType]] in the system.
  *
  * Used as a value object to maintain associations to with objects in the system.
  */
final case class ContainerTypeId(val id: String) extends IdentifiedValueObject[String] {}

object ContainerTypeId {

  // Do not want JSON to create a sub object, we just want it to be converted
  // to a single string
  implicit val containerTypeIdFormat: Format[ContainerTypeId] = new Format[ContainerTypeId] {

      override def writes(id: ContainerTypeId): JsValue = JsString(id.id)

      override def reads(json: JsValue): JsResult[ContainerTypeId] =
        Reads.StringReads.reads(json).map(ContainerTypeId.apply _)
    }

}
