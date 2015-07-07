package org.biobank.domain.participants

import org.biobank.domain._

class ParticipantAnnotationSpec extends StudyAnnotationSpec[ParticipantAnnotation] {

  def createAnnotation(annotationTypeId: AnnotationTypeId,
                       stringValue:      Option[String],
                       numberValue:      Option[String],
                       selectedValues:   List[AnnotationOption]) = {
    ParticipantAnnotation.create(annotationTypeId, stringValue, numberValue, selectedValues)
  }

  "A participant annotation" should {

    annotationBehaviour

  }

}
