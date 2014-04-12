package org.biobank.domain.validator

import org.biobank.domain._
import org.biobank.domain.study.StudyId

import org.slf4j.LoggerFactory
import scalaz._
import scalaz.Scalaz._

trait StudyValidator extends Validator {

  val log = LoggerFactory.getLogger(this.getClass)

  def validateId(id: StudyId): Validation[String, StudyId] = {
    val idString = id.toString
    if ((idString == null) || idString.isEmpty()) {
      "id is null or empty".failure
    } else {
      id.success
    }
  }
}
