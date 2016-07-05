package org.biobank.domain.processing

import org.biobank.domain.IdentifiedValueObject
import org.biobank.domain.participants.SpecimenId
import play.api.libs.json._

final case class ProcessingEventInputSpecimenId(id: String) extends IdentifiedValueObject[String]

object ProcessingEventInputSpecimenId {

  // Do not want JSON to create a sub object, we just want it to be converted
  // to a single string
  implicit val processingEventInputSpecimenIdReader: Reads[ProcessingEventInputSpecimenId] =
    (__).read[String].map( new ProcessingEventInputSpecimenId(_) )

  implicit val processingEventInputSpecimenIdWriter: Writes[ProcessingEventInputSpecimenId] =
    Writes{ (id: ProcessingEventInputSpecimenId) => JsString(id.id) }
}

final case class ProcessingEventInputSpecimen(id:                ProcessingEventInputSpecimenId,
                                              processingEventId: ProcessingEventId,
                                              specimenId:        SpecimenId)

object ProcessingEventInputSpecimen {

  implicit val processingEventInputSpecimenWrites: Writes[ProcessingEventInputSpecimen] =
    Json.writes[ProcessingEventInputSpecimen]

}
