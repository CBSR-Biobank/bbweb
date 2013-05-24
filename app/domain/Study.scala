package domain

import scalaz._
import Scalaz._

sealed abstract class Study extends Entity {
  def name: String
  def description: String

}

object Study {
  val invalidVersionMessage = "study %s: expected version %s doesn't match current version %s"

  def invalidVersion(studyId: Identity, expected: Long, current: Long) =
    DomainError(invalidVersionMessage format (studyId, expected, current))

  def requireVersion[T <: Study](study: T, expectedVersion: Option[Long]): DomainValidation[T] = {
    val id = study.id
    val version = study.version

    expectedVersion match {
      case Some(expected) if (version != expected) => invalidVersion(id, expected, version).fail
      case Some(expected) if (version == expected) => study.success
      case None => study.success
    }
  }

  def create(id: StudyId, name: String, description: String): DomainValidation[DisabledStudy] =
    DisabledStudy(id, version = 0L, name, description).success

}

case class DisabledStudy(id: StudyId, version: Long = -1, name: String, description: String)
  extends Study {

}