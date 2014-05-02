package org.biobank.domain

import org.biobank.infrastructure._

import scalaz._
import scalaz.Scalaz._

/**
  * Used to manage surrogate identity and optimistic concurrency versioning.
  *
  * This is a layer supertype.
  */
trait ConcurrencySafeEntity[T] extends IdentifiedDomainObject[T] {

  /** The current version of the object. Used for optimistic concurrency versioning. */
  val version: Long

  /** The version converted to a Option. */
  val versionOption = if (version < 0) None else Some(version)

  // FIXME: move these to another object
  //  val addedBy: UserId
  //  val timeAdded: Long
  //  val updatedBy: Option[UserId]
  //  val timeUpdated: Option[Long]

  protected def invalidVersion(expected: Long) =
    DomainError(
    s"""|${this.getClass}: expected version doesn't match current version: {
        |  id: $id,
	|  actualVersion: $version,
	|  expectedVersion: $expected
	|}""".stripMargin)

  /** Used for optimistic concurrency versioning.
    *
    * Fails if the expected version does not match the current version of the object.
    */
  protected def requireVersion(expectedVersion: Option[Long]): DomainValidation[ConcurrencySafeEntity[T]] = {
    expectedVersion match {
      case None => DomainError("expected version doesn't match current version").failNel
      case Some(expected) if (version != expected) => invalidVersion(expected).failNel
      case _ => this.success
    }
  }
}
