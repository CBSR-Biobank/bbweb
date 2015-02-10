package org.biobank.domain

import scalaz._
import scalaz.Scalaz._

/**
  * Trait for validation errors
  */
trait ValidationKey {
  def failureNel = this.toString.failureNel
  def nel = NonEmptyList(this.toString)
  def failure = this.toString.failure
}

object CommonValidations {

  case object IdRequired extends ValidationKey

  case object InvalidVersion extends ValidationKey

  case object NameRequired extends ValidationKey

  case object NonEmptyDescription extends ValidationKey

  def validateString(s: String, err: ValidationKey): DomainValidation[String] = {
    if ((s == null) || s.isEmpty()) err.failureNel else s.success
  }

  def validateString(s: String, minLength: Long, err: ValidationKey): DomainValidation[String] = {
    validateString(s, err).fold(
      err => err.failure,
      str => {
        if (str.length < minLength) {
          err.failureNel
        } else {
          str.successNel
        }
      }
    )

  }

  def validatePositiveNumber(number: Int, err: ValidationKey): DomainValidation[Int] = {
    if (number < 0) err.failureNel else number.success
  }

  def validatePositiveNumber(number: BigDecimal, err: ValidationKey): DomainValidation[BigDecimal] = {
    if (number < 0) err.failureNel else number.success
  }

  def validatePositiveNumberOption
    (maybeNumber: Option[BigDecimal], err: ValidationKey)
      : DomainValidation[Option[BigDecimal]] = {
    maybeNumber.fold {
      none[BigDecimal].successNel[String]
    } { number =>
      if (number < 0) {
        err.toString.failureNel[Option[BigDecimal]]
      } else {
        maybeNumber.successNel
      }
    }
  }

  def validateNonEmptyOption
    (maybeString: Option[String], err: ValidationKey)
      : DomainValidation[Option[String]] = {
    maybeString.fold {
      none[String].successNel[String]
    } { value =>
      if ((value == null) || value.isEmpty()) {
        err.toString.failureNel[Option[String]]
      } else {
        maybeString.successNel
      }
    }
  }

  def validateAndIncrementVersion(v: Long): DomainValidation[Long] =
    if (v < -1) InvalidVersion.failureNel else (v + 1).success

  def validateId[T <: IdentifiedValueObject[String]](
    id: T, err: ValidationKey): DomainValidation[T] = {
    validateString(id.id, err).fold(
      err => err.failure,
      idString => id.success)
  }

  def validateId[T <: IdentifiedValueObject[String]](id: T): DomainValidation[T] = {
    validateId(id, IdRequired)
  }

  def validateId[T <: IdentifiedValueObject[String]]
    (maybeId: Option[T], err: ValidationKey)
      : DomainValidation[Option[T]] = {
    maybeId.fold {
      none[T].successNel[String]
    } { id =>
      validateId(id, err).fold(
        err => err.toString.failureNel[Option[T]],
        id => some(id).success
      )
    }
  }

}
