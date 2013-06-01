package domain

trait Identity {
  def id: String

  override def equals(that: Any) = that match {
    case that: Identity => this.id.equals(that.id)
    case _ => false
  }

  override def toString = id
}