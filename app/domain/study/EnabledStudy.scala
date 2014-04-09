package domain.study

import domain.{ DomainError, DomainValidation }
import infrastructure.command._
import domain._

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