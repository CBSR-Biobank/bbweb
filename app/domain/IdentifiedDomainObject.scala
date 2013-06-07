package domain

trait IdentifiedDomainObject[T] {
  def id: T

  override def equals(that: Any) = that match {
    case that: IdentifiedDomainObject[_] => this.id.equals(that.id)
    case _ => false
  }

  override def toString = id.toString
}