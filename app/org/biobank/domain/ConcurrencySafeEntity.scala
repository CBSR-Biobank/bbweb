package org.biobank.domain

import org.joda.time.DateTime
import scalaz.Scalaz._

/**
  * Used to manage surrogate identity and optimistic concurrency versioning.
  *
  * This is a layer supertype.
  */
trait ConcurrencySafeEntity[T] extends IdentifiedDomainObject[T] {
  import org.biobank.CommonValidations._

  /** The current version of the object. Used for optimistic concurrency versioning. */
  val version: Long

  /** The version converted to a Option. */
  val versionOption = if (version < 0) None else Some(version)

  /** The date and time when this entity was added to the system. */
  val timeAdded: DateTime

  /** The date and time when this entity was last updated. */
  val timeModified: Option[DateTime]

  protected def invalidVersion(expected: Long) =
    InvalidVersion(s"${this.getClass.getSimpleName}: expected version doesn't match current version: id: $id, version: $version, expectedVersion: $expected")

  def requireVersion(expectedVersion: Long): DomainValidation[Boolean] = {
    if (this.version != expectedVersion) invalidVersion(expectedVersion).failureNel[Boolean]
    else true.successNel[String]
  }

}
