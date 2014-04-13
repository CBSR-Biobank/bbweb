package org.biobank.domain.validation

import org.biobank.domain._

import scalaz._
import scalaz.Scalaz._

private[domain] trait ValidationHelper {

  def validateStringId(id: String): Validation[String, String] = {
    if ((id == null) || id.isEmpty()) {
      "id is null or empty".failure
    } else {
      id.success
    }
  }

  def validateNonEmpty(fieldName: String, field: String): Validation[String, String] = {
    if ((field == null) || field.isEmpty()) {
      s"$fieldName is null or empty".fail
    } else {
      field.success
    }
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

  def validateAndIncrementVersion(v: Long): Validation[String, Long] =
    if (v >= -1) (v + 1).success else s"invalid version value: $v".failure

}
