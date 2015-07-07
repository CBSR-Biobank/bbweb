package org.biobank.domain.participants

import org.biobank.domain._

class CollectionEventAnnotationSpec extends StudyAnnotationSpec[CollectionEventAnnotation] {

  def createAnnotation(annotationTypeId: AnnotationTypeId,
                       stringValue:      Option[String],
                       numberValue:      Option[String],
                       selectedValues:   List[AnnotationOption]) = {
    CollectionEventAnnotation.create(annotationTypeId, stringValue, numberValue, selectedValues)
  }

  "A collectionEvent annotation" must {

    annotationBehaviour

  }

}
