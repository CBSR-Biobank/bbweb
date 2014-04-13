package org.biobank.domain.validation

import org.biobank.domain.study.StudyId

import scalaz._
import scalaz.Scalaz._

trait StudyValidationHelper extends ValidationHelper {

  def validateId(id: StudyId): Validation[String, StudyId] = {
    validateStringId(id.toString) match {
      case Success(idString) => id.success
      case Failure(err) => err.fail
    }
  }
}
