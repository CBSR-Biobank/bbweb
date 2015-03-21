package org.biobank.controllers.study

import org.biobank.fixture._
import org.biobank.domain._
import org.biobank.domain.study._
import org.biobank.fixture.ControllerFixture
import org.biobank.domain.JsonHelper._

import play.api.test.Helpers._
import play.api.test.WithApplication
import play.api.libs.json._
import org.scalatest.Tag
import com.typesafe.plugin._
import play.api.Play.current
import org.scalatestplus.play._
import org.joda.time.DateTime

class CeventAnnotTypeControllerSpec extends StudyAnnotTypeControllerSpec[CollectionEventAnnotationType] {
  import AnnotationValueType._
  import TestGlobal._

  override val uriPart = "ceannottypes"

  override def annotationTypeRepository = collectionEventAnnotationTypeRepository

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
