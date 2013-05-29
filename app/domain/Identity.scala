package domain

abstract class Identity {
  def id: String

  override def toString = id
}