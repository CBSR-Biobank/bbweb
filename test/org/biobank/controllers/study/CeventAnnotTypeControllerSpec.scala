package org.biobank.controllers.study

import org.biobank.domain._
import org.biobank.domain.study._

import org.scalatest.Tag
import play.api.Play.current
import org.joda.time.DateTime

class CeventAnnotTypeControllerSpec
    extends StudyAnnotTypeControllerSpec[CollectionEventAnnotationType] {

  override val uriPart = "ceannottypes"

  override def annotationTypeRepository = app.injector.instanceOf[CollectionEventAnnotationTypeRepository]

  override def createAnnotationType = factory.createCollectionEventAnnotationType

  override def annotationTypeCopyWithId(at: CollectionEventAnnotationType,
                                        id: AnnotationTypeId) =
    at.copy(id = id)

  override def annotationTypeCopyWithStudyId(at: CollectionEventAnnotationType,
                                             studyId: StudyId) =
    at.copy(studyId = studyId)

  override def annotationTypeCopyWithVersion(at: CollectionEventAnnotationType, version: Long) =
    at.copy(version = version)

  override def annotationTypeCopyWithName(at: CollectionEventAnnotationType, name: String) =
    at.copy(name = name)

  "Collection Event Annotation Type REST API" when {

    annotationTypeBehaviour

  }

}
