package org.biobank.domain.study

import org.biobank.domain.{ DomainError, DomainValidation }
import org.biobank.infrastructure.command._
import org.biobank.domain._

import scalaz._
import scalaz.Scalaz._

case class EnabledStudy(
  id: StudyId,
  version: Long = -1,
  name: String,
  description: Option[String])
  extends Study {

  override val status = "Ensabled"

  def disable: DomainValidation[DisabledStudy] =
    DisabledStudy(id, version + 1, name, description).success
}