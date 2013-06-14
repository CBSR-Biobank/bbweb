package domain

abstract class IdentifiedValueObject[T] extends IdentifiedDomainObject[T] {

  override def equals(other: Any) =
    other match {
      case that: IdentifiedValueObject[T] => this.id.equals(that.id)
      case _ => false
    }

  override def hashCode: Int = this.id.hashCode + 41
}