package org.biobank.domain.validator

import org.biobank.domain.study.StudyId

import scalaz._
import scalaz.Scalaz._

trait StudyValidator extends Validator {

  def validateId(id: StudyId): Validation[String, StudyId] = {
    validateStringId(id.toString) match {
      case Success(idString) => id.success
      case Failure(err) => err.fail
    }
  }
}
