package org.biobank.domain.validation

import org.biobank.domain._

import scalaz._
import scalaz.Scalaz._

private[domain] trait ValidationHelper {

  protected def validateStringId(id: String, errmsg: String): DomainValidation[String] = {
    if ((id == null) || id.isEmpty()) {
      errmsg.failNel
    } else {
      id.success
    }
  }

  protected def validateNonEmpty(value: String, errmsg: String): DomainValidation[String] = {
    if ((value == null) || value.isEmpty()) {
      errmsg.failNel
    } else {
      value.success
    }
  }

  protected def validatePositiveNumber(number: Int, errmsg: String): DomainValidation[Int] = {
    if (number < 0) {
      errmsg.failNel
    } else {
      number.success
    }
  }

  protected def validatePositiveNumber(
    number: BigDecimal, errmsg: String): DomainValidation[BigDecimal] = {
    if (number < 0) errmsg.failNel else number.success
  }

  protected def validatePositiveNumberOption(
    option: Option[BigDecimal], errmsg: String): DomainValidation[Option[BigDecimal]] = {
    option match {
      case Some(number) if (number < 0) => errmsg.failNel
      case _ => option.success
    }
  }

  protected def validateNonEmptyOption(
    option: Option[String],
    errmsg: String): DomainValidation[Option[String]] = {
    option match {
      case Some(value) if ((value == null) || value.isEmpty()) => errmsg.failNel
      case _ => option.success
    }
  }

  protected def validateAndIncrementVersion(v: Long): DomainValidation[Long] =
    if (v >= -1) (v + 1).success else s"invalid version value: $v".failNel

  protected def validateId[T <: IdentifiedValueObject[String]](id: T): DomainValidation[T] = {
    validateStringId(id.toString, "id is null or empty").fold(
      err => err.fail,
      idString => id.success)
  }

  protected def validateId[T <: IdentifiedValueObject[String]](
    idOption: Option[T]): DomainValidation[Option[T]] = {
    idOption match {
      case Some(id) =>
        validateId(id).fold(
          err => err.fail,
          id => idOption.success
        )
      case _ => idOption.success
    }
  }

}
