package org.biobank.domain.validation

import org.biobank.domain._

import scalaz._
import scalaz.Scalaz._

private[domain] trait ValidationHelper {

  protected def validateStringId(id: String, errmsg: String): Validation[String, String] = {
    if ((id == null) || id.isEmpty()) {
      errmsg.fail
    } else {
      id.success
    }
  }

  protected def validateNonEmpty(value: String, errmsg: String): Validation[String, String] = {
    if ((value == null) || value.isEmpty()) {
      errmsg.fail
    } else {
      value.success
    }
  }

  protected def validatePositiveNumber(number: Int, errmsg: String): Validation[String, Int] = {
    if (number < 0) {
      errmsg.fail
    } else {
      number.success
    }
  }

  protected def validatePositiveNumber(
    number: BigDecimal, errmsg: String): Validation[String, BigDecimal] = {
    if (number < 0) errmsg.fail else number.success
  }

  protected def validatePositiveNumberOption(
    option: Option[BigDecimal], errmsg: String): Validation[String, Option[BigDecimal]] = {
    option match {
      case Some(number) if (number < 0) => errmsg.fail
      case _ => option.success
    }
  }

  protected def validateNonEmptyOption(
    option: Option[String],
    errmsg: String): Validation[String, Option[String]] = {
    option match {
      case Some(value) if ((value == null) || value.isEmpty()) => errmsg.fail
      case _ => option.success
    }
  }

  protected def validateAndIncrementVersion(v: Long): Validation[String, Long] =
    if (v >= -1) (v + 1).success else s"invalid version value: $v".failure

}
