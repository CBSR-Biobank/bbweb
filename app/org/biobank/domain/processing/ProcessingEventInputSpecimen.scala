package org.biobank.domain.processing

import org.biobank.domain.IdentifiedValueObject
import org.biobank.domain.participants.SpecimenId
import play.api.libs.json._

case class ProcessingEventInputSpecimenId(id: String) extends IdentifiedValueObject[String]

object ProcessingEventInputSpecimenId {

  // Do not want JSON to create a sub object, we just want it to be converted
  // to a single string
  implicit val specimenIdReader = (__).read[String].map( new ProcessingEventInputSpecimenId(_) )
  implicit val specimenIdWriter = Writes{ (id: ProcessingEventInputSpecimenId) => JsString(id.id) }
}

case class ProcessingEventInputSpecimen(id:                ProcessingEventInputSpecimenId,
                                        processingEventId: ProcessingEventId,
                                        specimenId:        SpecimenId)

object ProcessingEventInputSpecimen {

    implicit val processingEventInputSpecimenWrites = Json.writes[ProcessingEventInputSpecimen]

}
