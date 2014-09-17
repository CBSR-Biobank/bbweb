package org.biobank.domain

import scalaz._
import scalaz.Scalaz._

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

  case object InvalidVersion extends ValidationKey

  case object NameRequired extends ValidationKey

  case object NonEmptyDescription extends ValidationKey

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
    if (v < -1) InvalidVersion.failNel else (v + 1).success

  def validateId[T <: IdentifiedValueObject[String]](
    id: T, err: ValidationKey): DomainValidation[T] = {
    validateString(id.id, err).fold(
      err => err.fail,
      idString => id.success)
  }

  def validateId[T <: IdentifiedValueObject[String]](id: T): DomainValidation[T] = {
    validateId(id, IdRequired)
  }

  def validateId[T <: IdentifiedValueObject[String]](
    idOption: Option[T], err: ValidationKey): DomainValidation[Option[T]] = {
    idOption match {
      case Some(id) =>
        validateId(id, err).fold(
          err => err.fail,
          id => idOption.success
        )
      case _ => idOption.success
    }
  }

}
