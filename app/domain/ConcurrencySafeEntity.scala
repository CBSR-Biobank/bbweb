package domain

import infrastructure._

import scalaz._
import scalaz.Scalaz._

abstract class ConcurrencySafeEntity[T]
  extends IdentifiedDomainObject[T] {

  val version: Long
  val versionOption = if (version == -1L) None else Some(version)

  // FIXME: move these to another object
  //  val addedBy: UserId
  //  val timeAdded: Long
  //  val updatedBy: Option[UserId]
  //  val timeUpdated: Option[Long]

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

  def update[S <: ConcurrencySafeEntity[_], T <: ConcurrencySafeEntity[_]](entity: DomainValidation[S],
    id: IdentifiedDomainObject[_], expectedVersion: Option[Long])(f: S => DomainValidation[T]): DomainValidation[T] =
    entity match {
      case Failure(x) => DomainError("no entity with id: %s" format id).fail
      case Success(entity) => for {
        current <- entity.requireVersion(expectedVersion)
        updated <- f(entity)
      } yield updated
    }

}
