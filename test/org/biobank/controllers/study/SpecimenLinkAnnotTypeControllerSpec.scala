package org.biobank.controllers.study

import org.biobank.domain._
import org.biobank.domain.study.{ Study, StudyId, SpecimenLinkAnnotationType }
import org.biobank.domain.study._

import org.joda.time.DateTime
import org.scalatest.Tag
import org.slf4j.LoggerFactory
import play.api.Play.current

class SpecimenLinkAnnotTypeControllerSpec
    extends StudyAnnotTypeControllerSpec[SpecimenLinkAnnotationType] {

  override def annotationTypeRepository = app.injector.instanceOf[SpecimenLinkAnnotationTypeRepository]

  override def createAnnotationType = factory.createSpecimenLinkAnnotationType

  override def annotationTypeCopyWithId(at: SpecimenLinkAnnotationType,
                                        id: AnnotationTypeId) =
    at.copy(id = id)

  override def annotationTypeCopyWithStudyId(at: SpecimenLinkAnnotationType,
                                             studyId: StudyId) =
    at.copy(studyId = studyId)

  override def annotationTypeCopyWithVersion(at: SpecimenLinkAnnotationType, version: Long) =
    at.copy(version = version)

  override def annotationTypeCopyWithName(at: SpecimenLinkAnnotationType, name: String) =
    at.copy(name = name)

  override val uriPart: String = "slannottypes"

  "Specimen Link Annotation Type REST API" when {

    annotationTypeBehaviour

  }

}
