package org.biobank.controllers.study

import org.biobank.domain.study.StudyId
import play.api.mvc.PathBindable.Parsing
import play.api.routing.sird._

object StudiesRouting {

  implicit object bindableStudyId extends Parsing[StudyId](
    StudyId.apply,
    _.id,
    (key: String, e: Exception) => s"$key is not a valid study Id"
  )

  val studyId = new PathBindableExtractor[StudyId]

}
