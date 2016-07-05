package org.biobank.domain.containers

import org.biobank.domain._

import play.api.libs.json._
import play.api.libs.json.Reads._

final case class ContainerSchemaId(val id: String) extends IdentifiedValueObject[String] {}

object ContainerSchemaId {

  implicit val containerSchemaIdReader: Reads[ContainerSchemaId] =
    (__ \ "id").read[String].map( new ContainerSchemaId(_) )

  implicit val containerSchemaIdWrite: Writes[ContainerSchemaId] =
    Writes{ (id: ContainerSchemaId) => JsString(id.id) }

}
