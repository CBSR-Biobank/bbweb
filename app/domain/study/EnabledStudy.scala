package domain.study

import infrastructure.{ DomainError, DomainValidation, ReadWriteRepository }
import infrastructure.commands._

import scalaz._
import scalaz.Scalaz._

case class EnabledStudy(id: StudyId, version: Long = -1, name: String, description: String)
  extends Study {

  def disable: DomainValidation[DisabledStudy] =
    DisabledStudy(id, version + 1, name, description).success
}