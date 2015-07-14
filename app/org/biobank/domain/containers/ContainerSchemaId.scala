package org.biobank.domain.containers

import org.biobank.domain._

import play.api.libs.json._
import play.api.libs.json.Reads._

case class ContainerSchemaId(val id: String) extends IdentifiedValueObject[String] {}

object ContainerSchemaId {

  implicit val containerSchemaIdReader = (__ \ "id").read[String].map( new ContainerSchemaId(_) )
  implicit val containerSchemaIdWrite = Writes{ (id: ContainerSchemaId) => JsString(id.id) }

}
