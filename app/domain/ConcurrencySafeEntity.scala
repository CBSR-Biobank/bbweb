package domain

import scalaz._
import scalaz.Scalaz._

abstract class ConcurrencySafeEntity[T] extends IdentifiedDomainObject[T] {
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

object Entity {

  def update[S <: ConcurrencySafeEntity[_], T <: ConcurrencySafeEntity[_]](entity: Option[S],
    id: IdentifiedDomainObject[_], expectedVersion: Option[Long])(f: S => DomainValidation[T]): DomainValidation[T] =
    entity match {
      case None => DomainError("no entity with id: %s" format id).fail
      case Some(entity) => for {
        current <- entity.requireVersion(expectedVersion)
        updated <- f(entity)
      } yield updated
    }

}
