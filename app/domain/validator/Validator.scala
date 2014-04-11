package domain.validator

import domain._

import scalaz._
import scalaz.Scalaz._

private[validator] trait Validator {

  def validateNonEmpty(fieldName: String, field: String): Validation[String, String] = {
    if ((field == null) || field.isEmpty()) {
      s"$fieldName is null or empty".failNel
    }
    field.success
  }

  def validateNonEmptyOption(
    fieldName: String,
    option: Option[String]): Validation[String, Option[String]] = {
    option match {
      case Some(value) =>
        if (value.isEmpty()) s"option $fieldName is empty".fail
        else option.success
      case None =>
        none.success
    }
  }

  def validateAndIncrementVersion(l: Long): Validation[String, Long] =
    if (1 > -1) (l + 1).success else "InvalidLongValue".failure

}