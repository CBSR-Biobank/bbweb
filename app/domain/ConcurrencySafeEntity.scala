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

  val invalidVersionMessage = "%s: expected version doesn't match current version: { id: %s, actualVersion: %d, expectedVersion: %d }"

  def invalidVersion(expected: Long) =
    DomainError(invalidVersionMessage.format(this.getClass.getSimpleName, id, version, expected))

  def requireVersion(expectedVersion: Option[Long]): DomainValidation[ConcurrencySafeEntity[T]] = {
    expectedVersion match {
      case Some(expected) if (version != expected) => invalidVersion(expected).failNel
      case Some(expected) if (version == expected) => this.success
      case None => this.success
    }
  }
}

object Entity {

  def update[S <: ConcurrencySafeEntity[_], T <: ConcurrencySafeEntity[_]](entity: DomainValidation[S],
    id: IdentifiedDomainObject[_], expectedVersion: Option[Long])(f: S => DomainValidation[T]): DomainValidation[T] =
    entity match {
      case Failure(x) => DomainError("no entity with id: %s" format id).failNel
      case Success(entity) => for {
        current <- entity.requireVersion(expectedVersion)
        updated <- f(entity)
      } yield updated
    }

}
