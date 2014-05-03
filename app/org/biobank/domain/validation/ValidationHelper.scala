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

  //  FIXME: find a better spot for this function
  protected def validateId(id: ContainerTypeId): DomainValidation[ContainerTypeId] = {
    validateStringId(id.toString, "collection event type id is null or empty") match {
      case Success(idString) => id.success
      case Failure(err) => err.fail
    }
  }

  //  FIXME: find a better spot for this function. Paired with function above.
  protected def validateId(
    idOption: Option[ContainerTypeId]): DomainValidation[Option[ContainerTypeId]] = {
    idOption match {
      case Some(id) =>
        validateId(id) match {
          case Success(id) => idOption.success
          case Failure(err) => err.fail
        }
      case _ => idOption.success
    }
  }

}
