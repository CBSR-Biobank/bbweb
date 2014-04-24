package org.biobank.domain

import org.biobank.domain._
import org.biobank.domain.study._
import org.biobank.fixture.NameGenerator

class Factory(nameGenerator: NameGenerator) {

  val domainObjects: Map[String, ConcurrencySafeEntity[_]] = Map.empty

  def createDisabledStudy: DisabledStudy = {
    val id = StudyId(nameGenerator.next[Study])
    val name = nameGenerator.next[Study]
    val description = Some(nameGenerator.next[Study])

    val study = DisabledStudy.create(id, -1L, name, description) | null
    domainObjects + ("DisabledStudy" -> study)
    study
  }

  def defaultDisabledStudy: DisabledStudy = {
    domainObjects get "DisabledStudy" match {
      case Some(obj) => obj match {
	case study: DisabledStudy => study
	case _ => throw new Error
      }
      case None => createDisabledStudy
    }
  }

}
