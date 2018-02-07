package org.biobank.domain

import org.biobank.ValidationKey
import scalaz.Scalaz._

object DomainValidations {

  import org.biobank.CommonValidations._

  case object IdRequired extends ValidationKey

  case object InvalidVersion extends ValidationKey

  case object NameRequired extends ValidationKey

  case object InvalidDescription extends ValidationKey

  case object InvalidAccessItemId extends ValidationKey

  case object InvalidUserId extends ValidationKey

  case object InvalidStudyId extends ValidationKey

  case object InvalidCentreId extends ValidationKey

  case object CentreIdRequired extends ValidationKey

  case object PasswordRequired extends ValidationKey

  def validateVersion(v: Long): DomainValidation[Long] =
    if (v < 0) InvalidVersion.failureNel[Long] else v.successNel[String]

  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  def validateId[T <: IdentifiedValueObject[String]](id: T, err: ValidationKey): DomainValidation[T] = {
    validateNonEmptyString(id.id, err).map(_ => id)
  }

  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  def validateId[T <: IdentifiedValueObject[String]](id: T): DomainValidation[T] = {
    validateId(id, IdRequired)
  }

  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  def validateIdOption[T <: IdentifiedValueObject[String]](id: Option[T], err: ValidationKey)
      : DomainValidation[Option[T]] = {
    id match {
      case Some(i) => validateId(i, err).fold(
        err => err.failure[Option[T]],
        _   => id.successNel[String]
      )
      case None    => id.successNel[String]
    }
  }

  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  def validateIdOption[T <: IdentifiedValueObject[String]](id: Option[T])
      : DomainValidation[Option[T]] = {
    validateIdOption(id, IdRequired)
  }

}
