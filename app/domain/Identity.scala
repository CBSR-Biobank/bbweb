package domain

trait Identity {
  def id: String

  override def toString = id
}