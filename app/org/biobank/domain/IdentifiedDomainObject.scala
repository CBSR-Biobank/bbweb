package org.biobank.domain

trait IdentifiedDomainObject[T] {
  val id: T

  override def equals(that: Any) = that match {
    case that: IdentifiedDomainObject[_] => this.id.equals(that.id)
    case _ => false
  }

  override def hashCode: Int = this.id.hashCode + 41

  override def toString = id.toString
}