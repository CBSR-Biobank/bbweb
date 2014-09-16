package org.biobank.domain

import scalaz._
import scalaz.Scalaz._

private[domain] trait Validations {

  @deprecated("no longer used", "2014-09-16")
  protected def validateNonEmptyOption(
    option: Option[String],
    errmsg: String): DomainValidation[Option[String]] = {
    option match {
      case Some(value) if ((value == null) || value.isEmpty()) => errmsg.failNel
      case _ => option.success
    }
  }

  @deprecated("no longer used", "2014-09-16")
  protected def validateAndIncrementVersion(v: Long): DomainValidation[Long] =
    if (v >= -1) (v + 1).success else s"invalid version value: $v".failNel

  @deprecated("no longer used", "2014-09-16")
  protected def validateId[T <: IdentifiedValueObject[String]](id: T): DomainValidation[T] = {
    validateStringId(id.toString, "id is null or empty").fold(
      err => err.fail,
      idString => id.success)
  }

  @deprecated("no longer used", "2014-09-16")
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

  @deprecated("no longer used", "2014-09-16")
  protected def validateStringId(id: String, errmsg: String): DomainValidation[String] = {
    if ((id == null) || id.isEmpty()) {
      errmsg.failNel
    } else {
      id.success
    }
  }

  @deprecated("no longer used", "2014-09-16")
  protected def validateNonEmpty(value: String, errmsg: String): DomainValidation[String] = {
    if ((value == null) || value.isEmpty()) {
      errmsg.failNel
    } else {
      value.success
    }
  }

  @deprecated("no longer used", "2014-09-16")
  protected def validatePositiveNumber(number: Int, errmsg: String): DomainValidation[Int] = {
    if (number < 0) {
      errmsg.failNel
    } else {
      number.success
    }
  }

  @deprecated("no longer used", "2014-09-16")
  protected def validatePositiveNumber(
    number: BigDecimal, errmsg: String): DomainValidation[BigDecimal] = {
    if (number < 0) errmsg.failNel else number.success
  }

  @deprecated("no longer used", "2014-09-16")
  protected def validatePositiveNumberOption(
    option: Option[BigDecimal], errmsg: String): DomainValidation[Option[BigDecimal]] = {
    option match {
      case Some(number) if (number < 0) => errmsg.failNel
      case _ => option.success
    }
  }
}

/**
  * Trait for validation errors
  */
trait ValidationKey {
  def failNel = this.toString.failureNel
  def nel = NonEmptyList(this.toString)
  def failure = this.toString.failure
}

object CommonValidations {

  case object IdRequired extends ValidationKey

  def validateString(s: String, err: ValidationKey): DomainValidation[String] = {
    if ((s == null) || s.isEmpty()) err.failNel else s.success
  }

  def validatePositiveNumber(number: Int, err: ValidationKey): DomainValidation[Int] = {
    if (number < 0) err.failNel else number.success
  }

  def validatePositiveNumber(number: BigDecimal, err: ValidationKey): DomainValidation[BigDecimal] = {
    if (number < 0) err.failNel else number.success
  }

  def validatePositiveNumberOption(
    option: Option[BigDecimal],
    err: ValidationKey): DomainValidation[Option[BigDecimal]] = {
    option match {
      case Some(number) if (number < 0) => err.failNel
      case _ => option.success
    }
  }

  def validateNonEmptyOption(
    option: Option[String],
    err: ValidationKey): DomainValidation[Option[String]] = {
    option match {
      case Some(value) if ((value == null) || value.isEmpty()) => err.failNel
      case _ => option.success
    }
  }

  def validateAndIncrementVersion(v: Long): DomainValidation[Long] =
    if (v < -1) s"invalid version value: $v".failNel else (v + 1).success

  def validateId[T <: IdentifiedValueObject[String]](id: T): DomainValidation[T] = {
    validateString(id.toString, IdRequired).fold(
      err => err.failure,
      idString => id.success)
  }

}
