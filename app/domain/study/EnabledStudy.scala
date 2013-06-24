package domain.study

import infrastructure.{ DomainError, DomainValidation, ReadWriteRepository }
import infrastructure.commands._
import domain._

import scalaz._
import scalaz.Scalaz._

case class EnabledStudy(
  id: StudyId,
  version: Long = -1,
  name: String,
  description: Option[String],
  addedBy: UserId,
  timeAdded: Long,
  updatedBy: Option[UserId],
  timeUpdated: Option[Long])
  extends Study {

  override val status = "Ensabled"

  def disable: DomainValidation[DisabledStudy] =
    DisabledStudy(id, version + 1, name, description, addedBy, timeAdded, updatedBy, timeUpdated).success
}