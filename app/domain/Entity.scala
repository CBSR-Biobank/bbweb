package domain

abstract class Entity {
  def id: Identity

  def version: Long
  def versionOption = if (version == -1L) None else Some(version)

}