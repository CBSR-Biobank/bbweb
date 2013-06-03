package domain

import scalaz._
import scalaz.Scalaz._

abstract class ConcurrencySafeEntity[T <: Identity] {
  def id: T
  def version: Long
  def versionOption = if (version == -1L) None else Some(version)

  // FIXME: add creation time and update time
  //def creationTime
  //def updateTime

  val invalidVersionMessage = "expected version %s doesn't match current version %s"

  def invalidVersion(expected: Long) =
    DomainError(invalidVersionMessage format (id, expected, version))

  def requireVersion(expectedVersion: Option[Long]): DomainValidation[ConcurrencySafeEntity[T]] = {
    expectedVersion match {
      case Some(expected) if (version != expected) => invalidVersion(expected).fail
      case Some(expected) if (version == expected) => this.success
      case None => this.success
    }
  }

}
