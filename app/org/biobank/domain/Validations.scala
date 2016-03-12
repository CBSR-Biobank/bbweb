package org.biobank.domain

import org.biobank.ValidationKey
import scala.util.control.Exception._
import scalaz.Scalaz._

object CommonValidations {

  case object IdRequired extends ValidationKey

  case object InvalidVersion extends ValidationKey

  case object InvalidName extends ValidationKey

  case object NameRequired extends ValidationKey

  case object InvalidDescription extends ValidationKey

  case object NonEmptyString extends ValidationKey

  case object NonEmptyStringOption extends ValidationKey

  case object InvalidNumberString extends ValidationKey

  case object CentreIdRequired extends ValidationKey

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

  def validateNumberString(s: String): DomainValidation[String] = {
    validateString(s, InvalidNumberString).fold(
      err => err.failure,
      str => {
        catching(classOf[NumberFormatException]).opt(str.toFloat) match {
          case None => InvalidNumberString.failureNel
          case _ => str.successNel
        }
      }
    )
  }

  def validateNumberStringOption(maybeString: Option[String]): DomainValidation[Option[String]] = {
    maybeString.fold {
      none[String].successNel[String]
    } { str =>
      validateNumberString(str).fold(
        err => err.failure,
        numstr => Some(numstr).success
      )
    }
  }

  def validateNonEmptyOption(maybeString: Option[String], err: ValidationKey)
      : DomainValidation[Option[String]] = {
    maybeString.fold {
      none[String].successNel[String]
    } { value =>
      validateString(value, err).fold(
        err => err.failure,
        str => Some(str).success
      )
    }
  }

  def validateMinimum(number: Int, min: Int, err: ValidationKey): DomainValidation[Int] = {
    if (number < min) err.failureNel else number.success
  }

  def validatePositiveNumber(number: Int, err: ValidationKey): DomainValidation[Int] = {
    validateMinimum(number, 0, err)
  }

  def validatePositiveNumber(number: BigDecimal, err: ValidationKey): DomainValidation[BigDecimal] = {
    if (number < 0) err.failureNel else number.success
  }

  def validatePositiveNumberOption(maybeNumber: Option[BigDecimal], err: ValidationKey)
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

  def validateVersion(v: Long): DomainValidation[Long] =
    if (v < 0) InvalidVersion.failureNel else v.success

  def validateId[T <: IdentifiedValueObject[String]](id: T, err: ValidationKey): DomainValidation[T] = {
    validateString(id.id, err).map(_ => id)
  }

  def validateId[T <: IdentifiedValueObject[String]](maybeId: Option[T], err: ValidationKey)
      : DomainValidation[Option[T]] = {
    maybeId.fold {
      none[T].successNel[String]
    } { id =>
      validateId(id, err).map(_ => some(id))
    }
  }

  def validateId[T <: IdentifiedValueObject[String]](id: T): DomainValidation[T] = {
    validateId(id, IdRequired)
  }

}
