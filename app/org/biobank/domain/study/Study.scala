package org.biobank.domain.study

import org.biobank.domain._

import scalaz._
import scalaz.Scalaz._

abstract class Study extends ConcurrencySafeEntity[StudyId]
  with HasName with HasDescriptionOption {
  val name: String
  val description: Option[String]

  override def toString =
    "{ id:%s, version: %d, name:%s, description:%s }" format (id, version, name, description)

  val status: String = "invalid"

}

object Study {

  def noSuchStudy(studyId: StudyId) =
    DomainError("no study with id: %s" format studyId)

  def notDisabledError(name: String) =
    DomainError("study is not disabled: %s" format name)

  def notEnabledError(name: String) =
    DomainError("study is not enabled: %s" format name)
}
