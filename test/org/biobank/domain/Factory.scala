package org.biobank.domain

import org.biobank.fixture.NameGenerator
import org.biobank.domain._
import org.biobank.domain.study._
import org.slf4j.LoggerFactory
import scalaz._
import scalaz.Scalaz._

class Factory(nameGenerator: NameGenerator) {
  this: RepositoryComponentImpl =>

  val log = LoggerFactory.getLogger(this.getClass)

  var domainObjects: Map[Class[_ <: ConcurrencySafeEntity[_]], ConcurrencySafeEntity[_]] = Map.empty

  def createDisabledStudy: DisabledStudy = {
    val id = studyRepository.nextIdentity
    val name = nameGenerator.next[Study]
    val description = Some(nameGenerator.next[Study])

    val validation = DisabledStudy.create(id, -1L, name, description)
    if (validation.isFailure) {
      throw new Error
    }

    val study = validation | null
    domainObjects = domainObjects + (classOf[DisabledStudy] -> study)
    study
  }

  def createEnabledStudy: EnabledStudy = {
    val disabledStudy = defaultDisabledStudy
    val enabledStudy = disabledStudy.enable(disabledStudy.versionOption, 1, 1) | null
    domainObjects = domainObjects + (classOf[EnabledStudy] -> enabledStudy)
    domainObjects = domainObjects - classOf[DisabledStudy]
    enabledStudy
  }

  def createSpecimenGroup: SpecimenGroup = {
    val sgId = specimenGroupRepository.nextIdentity
    val name = nameGenerator.next[SpecimenGroup]
    val description = Some(nameGenerator.next[SpecimenGroup])
    val units = nameGenerator.next[String]
    val anatomicalSourceType = AnatomicalSourceType.Blood
    val preservationType = PreservationType.FreshSpecimen
    val preservationTempType = PreservationTemperatureType.Minus80celcius
    val specimenType = SpecimenType.FilteredUrine

    val disabledStudy = defaultDisabledStudy
    val validation = SpecimenGroup.create(disabledStudy.id, sgId, -1L,
      name, description, units, anatomicalSourceType, preservationType, preservationTempType,
      specimenType)
    if (validation.isFailure) {
      throw new Error
    }

    val specimenGroup = validation | null
    domainObjects = domainObjects + (classOf[SpecimenGroup] -> specimenGroup)
    specimenGroup
  }

  def createCollectionEventType: CollectionEventType = {
    val ceventTypeId = collectionEventTypeRepository.nextIdentity
    val name = nameGenerator.next[CollectionEventType]
    val description = Some(nameGenerator.next[CollectionEventType])

    val disabledStudy = defaultDisabledStudy
    val validation = CollectionEventType.create(disabledStudy.id, ceventTypeId, -1L, name,
      description, true, List.empty, List.empty)
    if (validation.isFailure) {
      throw new Error
    }

    val ceventType = validation | null
    domainObjects = domainObjects + (classOf[CollectionEventType] -> ceventType)
    ceventType
  }

  def defaultDisabledStudy: DisabledStudy = {
    domainObjects get classOf[DisabledStudy] match {
      case Some(obj) => obj match {
	case study: DisabledStudy =>
	  study
	case _ => throw new Error
      }
      case None => createDisabledStudy
    }
  }

  def defaultEnabledStudy: EnabledStudy = {
    domainObjects get classOf[EnabledStudy] match {
      case Some(obj) => obj match {
	case study: EnabledStudy => study
	case _ => throw new Error
      }
      case None => createEnabledStudy
    }
  }

  def defaultSpecimenGroup: SpecimenGroup = {
    domainObjects get classOf[SpecimenGroup] match {
      case Some(obj) => obj match {
	case sg: SpecimenGroup => sg
	case _ => throw new Error
      }
      case None => createSpecimenGroup
    }
  }

  def defaultCollectionEventType: CollectionEventType = {
    domainObjects get classOf[CollectionEventType] match {
      case Some(obj) => obj match {
	case sg: CollectionEventType => sg
	case _ => throw new Error
      }
      case None => createCollectionEventType
    }
  }

}
