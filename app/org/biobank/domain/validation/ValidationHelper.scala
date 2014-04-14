package org.biobank.domain.validation

import org.biobank.domain._

import scalaz._
import scalaz.Scalaz._

private[domain] trait ValidationHelper {

  def validateStringId(id: String, errmsg: String): Validation[String, String] = {
    if ((id == null) || id.isEmpty()) {
      errmsg.fail
    } else {
      id.success
    }
  }

  def validateNonEmpty(field: String, errmsg: String): Validation[String, String] = {
    if ((field == null) || field.isEmpty()) {
      errmsg.fail
    } else {
      field.success
    }
  }

  def validatePositiveNumber(number: Int, errmsg: String): Validation[String, Int] = {
    if (number < 0) {
      errmsg.fail
    } else {
      number.success
    }
  }

  def validatePositiveNumber(number: BigDecimal, errmsg: String): Validation[String, BigDecimal] = {
    if (number < 0) {
      errmsg.fail
    } else {
      number.success
    }
  }


  def validateNonEmptyOption(
    option: Option[String],
    errmsg: String): Validation[String, Option[String]] = {
    option match {
      case Some(value) =>
        if (value.isEmpty()) errmsg.fail
        else option.success
      case None =>
        none.success
    }
  }

  def validateAndIncrementVersion(v: Long): Validation[String, Long] =
    if (v >= -1) (v + 1).success else s"invalid version value: $v".failure

}
