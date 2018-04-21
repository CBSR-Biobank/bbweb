package org.biobank.domain

import java.time.OffsetDateTime
import play.api.libs.json._
import play.api.libs.json._
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
  val versionOption: Option[Long] = if (version < 0) None else Some(version)

  /** The date and time when this entity was added to the system. */
  val timeAdded: OffsetDateTime

  /** The date and time when this entity was last updated. */
  val timeModified: Option[OffsetDateTime]

  protected def invalidVersion(expected: Long) =
    InvalidVersion(s"${this.getClass.getSimpleName}: expected version doesn't match current version: id: $id, version: $version, expectedVersion: $expected")

  def requireVersion(expectedVersion: Long): DomainValidation[Boolean] = {
    if (this.version != expectedVersion) invalidVersion(expectedVersion).failureNel[Boolean]
    else true.successNel[String]
  }

}

object ConcurrencySafeEntity {

  @SuppressWarnings(Array("org.wartremover.warts.Option2Iterable"))
  def toJson[T <: ConcurrencySafeEntity[_]](entity: T): JsObject = {
    Json.obj("id"           -> entity.id.toString,
             "version"      -> entity.version,
             "timeAdded"    -> entity.timeAdded) ++
    JsObject(
      Seq[(String, JsValue)]() ++
        entity.timeModified.map(t => "timeModified" -> Json.toJson(t)))
  }

}
