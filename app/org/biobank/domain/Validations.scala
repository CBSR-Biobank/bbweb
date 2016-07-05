package org.biobank.domain

import org.biobank.ValidationKey
import scala.util.control.Exception._
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

object CommonValidations {

  case object IdRequired extends ValidationKey

  case object InvalidVersion extends ValidationKey

  case object InvalidName extends ValidationKey

  case object NameRequired extends ValidationKey

  case object InvalidDescription extends ValidationKey

  case object NonEmptyString extends ValidationKey

  case object NonEmptyStringOption extends ValidationKey

  case object InvalidNumberString extends ValidationKey {
      override val toString = "InvalidNumberString"
    }

  case object CentreIdRequired extends ValidationKey

  @SuppressWarnings(Array("org.wartremover.warts.Overloading", "org.wartremover.warts.Null"))
  def validateString(s: String, err: ValidationKey): DomainValidation[String] = {
    if ((s == null) || s.isEmpty()) err.failureNel[String] else s.successNel[String]
  }

  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  def validateString(str: String, minLength: Long, err: ValidationKey): DomainValidation[String] = {
    for {
      valid <- validateString(str, err)
      lenValid <- {
        if (str.length < minLength) err.failureNel[String]
        else str.successNel[String]
      }
    } yield lenValid
  }

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  def validateNumberString(str: String): DomainValidation[String] = {
    for {
      valid <- validateString(str, InvalidNumberString)
      number <- {
        catching[String](classOf[NumberFormatException]).opt(str.toFloat)
          .toSuccessNel(InvalidNumberString.toString)
      }
    } yield valid
  }

  def validateNumberStringOption(maybeString: Option[String]): DomainValidation[Option[String]] = {
    maybeString.fold {
      none[String].successNel[String]
    } { str =>
      validateNumberString(str).fold(
        err => err.failure[Option[String]],
        numstr => Some(numstr).successNel[String]
      )
    }
  }

  def validateNonEmptyOption(maybeString: Option[String], err: ValidationKey)
      : DomainValidation[Option[String]] = {
    maybeString.fold {
      none[String].successNel[String]
    } { value =>
      validateString(value, err).fold(
        err => err.failure[Option[String]],
        str => Some(str).successNel[String]
      )
    }
  }

  def validateMinimum(number: Int, min: Int, err: ValidationKey): DomainValidation[Int] = {
    if (number < min) err.failureNel[Int] else number.successNel[String]
  }

  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  def validatePositiveNumber(number: Int, err: ValidationKey): DomainValidation[Int] = {
    validateMinimum(number, 0, err)
  }

  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  def validatePositiveNumber(number: BigDecimal, err: ValidationKey): DomainValidation[BigDecimal] = {
    if (number < 0) err.failureNel[BigDecimal] else number.successNel[String]
  }

  def validatePositiveNumberOption(maybeNumber: Option[BigDecimal], err: ValidationKey)
      : DomainValidation[Option[BigDecimal]] = {
    maybeNumber.fold {
      none[BigDecimal].successNel[String]
    } { number =>
      if (number < 0) {
        err.failureNel[Option[BigDecimal]]
      } else {
        maybeNumber.successNel[String]
      }
    }
  }

  def validateVersion(v: Long): DomainValidation[Long] =
    if (v < 0) InvalidVersion.failureNel[Long] else v.successNel[String]

  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  def validateId[T <: IdentifiedValueObject[String]](id: T, err: ValidationKey): DomainValidation[T] = {
    validateString(id.id, err).map(_ => id)
  }

  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  def validateId[T <: IdentifiedValueObject[String]](maybeId: Option[T], err: ValidationKey)
      : DomainValidation[Option[T]] = {
    maybeId.fold {
      none[T].successNel[String]
    } { id =>
      validateId(id, err).map(_ => some(id))
    }
  }

  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  def validateId[T <: IdentifiedValueObject[String]](id: T): DomainValidation[T] = {
    validateId(id, IdRequired)
  }

}
