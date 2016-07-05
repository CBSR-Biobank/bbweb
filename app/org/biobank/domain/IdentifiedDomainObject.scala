package org.biobank.domain

/**
  * An object with a unique id.
  */
@SuppressWarnings(Array("org.wartremover.warts.ToString"))
trait IdentifiedDomainObject[T] {

  /** The unique ID for this object. */
  val id: T

  override def equals(that: Any) = that match {
    case that: IdentifiedDomainObject[_] => this.id == that.id
    case _ => false
  }

  override def hashCode: Int = this.id.hashCode + 41

  override def toString = id.toString
}
