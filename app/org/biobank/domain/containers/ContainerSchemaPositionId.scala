package org.biobank.domain.containers

import org.biobank.domain._

import play.api.libs.json._
import play.api.libs.json.Reads._

/** Identifies a unique [[Specimen]] in the system.
  *
  * Used as a value object to maintain associations to with objects in the system.
  */
final case class ContainerSchemaPositionId(id: String) extends IdentifiedValueObject[String]

object ContainerSchemaPositionId {

  // Do not want JSON to create a sub object, we just want it to be converted
  // to a single string
  implicit val containerSchemaPositionIdReader: Reads[ContainerSchemaPositionId] =
    (__).read[String].map( new ContainerSchemaPositionId(_) )

  implicit val containerSchemaPositionIdWriter: Writes[ContainerSchemaPositionId] =
    Writes{ (id: ContainerSchemaPositionId) => JsString(id.id) }

}
