package domain

abstract class Entity[T <: Identity] {
  def id: T
  def version: Long
  def versionOption = if (version == -1L) None else Some(version)

  //def creationTime
  //def updateTime

}