package org.biobank.domain

import org.biobank.fixture.NameGenerator
import org.biobank.domain._
import org.biobank.domain.user._
import org.biobank.domain.study._
import org.biobank.domain.centre._
import org.biobank.infrastructure.{
  CollectionEventTypeAnnotationTypeData,
  CollectionEventTypeSpecimenGroupData,
  SpecimenLinkTypeAnnotationTypeData
}
import org.slf4j.LoggerFactory
import scala.reflect.ClassTag
import scala.reflect._
import org.joda.time.DateTime
import scalaz._
import scalaz.Scalaz._

class Factory {

  val log = LoggerFactory.getLogger(this.getClass)

  val nameGenerator = new NameGenerator(this.getClass)

  var domainObjects: Map[Class[_], _] = Map.empty

  def createRegisteredUser: RegisteredUser = {
    val version = -1L
    val name = nameGenerator.next[User]
    val email = nameGenerator.nextEmail[User]
    val id = UserId(email)
    val password = nameGenerator.next[User]
    val salt = nameGenerator.next[User]
    val avatarUrl = Some("http://test.com/")

    RegisteredUser.create(id, version, DateTime.now, name, email, password, salt, avatarUrl).fold(
      err => throw new Error(err.list.mkString),
      registeredUser => {
        domainObjects = domainObjects + (classOf[RegisteredUser] -> registeredUser)
        registeredUser
      }
    )
  }

  def createActiveUser: ActiveUser = {
    val registeredUser = defaultRegisteredUser
    registeredUser.activate.fold(
      err => throw new Error(err.list.mkString),
      activeUser => {
        domainObjects = domainObjects + (classOf[ActiveUser] -> activeUser)
        activeUser
      }
    )
  }

  def createLockedUser: LockedUser = {
    val activeUser = defaultActiveUser
    activeUser.lock.fold(
      err => throw new Error(err.list.mkString),
      lockedUser => {
        domainObjects = domainObjects + (classOf[LockedUser] -> lockedUser)
        lockedUser
      }
    )
  }
  def createDisabledStudy: DisabledStudy = {
    val id = StudyId(nameGenerator.next[Study])
    val name = nameGenerator.next[Study]
    val description = Some(nameGenerator.next[Study])

    DisabledStudy.create(id, -1L, DateTime.now, name, description).fold(
      err => throw new Error(err.list.mkString),
      study => {
        domainObjects = domainObjects + (classOf[DisabledStudy] -> study)
        study
      }
    )
  }

  def createEnabledStudy: EnabledStudy = {
    val disabledStudy = defaultDisabledStudy
    val enabledStudy = disabledStudy.enable(1, 1) | null
    domainObjects = domainObjects + (classOf[EnabledStudy] -> enabledStudy)
    domainObjects = domainObjects - classOf[DisabledStudy]
    enabledStudy
  }

  def createSpecimenGroup: SpecimenGroup = {
    val sgId = SpecimenGroupId(nameGenerator.next[SpecimenGroup])
    val name = nameGenerator.next[SpecimenGroup]
    val description = Some(nameGenerator.next[SpecimenGroup])
    val units = nameGenerator.next[String]
    val anatomicalSourceType = AnatomicalSourceType.Blood
    val preservationType = PreservationType.FreshSpecimen
    val preservationTempType = PreservationTemperatureType.Minus80celcius
    val specimenType = SpecimenType.FilteredUrine

    val disabledStudy = defaultDisabledStudy
    SpecimenGroup.create(disabledStudy.id, sgId, -1L, DateTime.now,
      name, description, units, anatomicalSourceType, preservationType, preservationTempType,
      specimenType).fold(
      err => throw new Error(err.list.mkString),
        specimenGroup => {
          domainObjects = domainObjects + (classOf[SpecimenGroup] -> specimenGroup)
          specimenGroup
        }
    )
  }

  def createCollectionEventType: CollectionEventType = {
    val ceventTypeId = CollectionEventTypeId(nameGenerator.next[CollectionEventType])
    val name = nameGenerator.next[CollectionEventType]
    val description = Some(nameGenerator.next[CollectionEventType])

    val disabledStudy = defaultDisabledStudy
    CollectionEventType.create(
      disabledStudy.id, ceventTypeId, -1L, DateTime.now, name, description, true, List.empty, List.empty).fold(
      err => throw new Error(err.list.mkString),
        ceventType => {
          domainObjects = domainObjects + (classOf[CollectionEventType] -> ceventType)
          ceventType
        }
    )
  }

  def createCollectionEventAnnotationType: CollectionEventAnnotationType = {
    val id = AnnotationTypeId(nameGenerator.next[CollectionEventAnnotationType])
    val name = nameGenerator.next[CollectionEventAnnotationType]
    val description = Some(nameGenerator.next[CollectionEventAnnotationType])
    val options = Some(Seq(
      nameGenerator.next[String],
      nameGenerator.next[String]))

    val disabledStudy = defaultDisabledStudy
    CollectionEventAnnotationType.create(
      disabledStudy.id, id, -1L, DateTime.now, name, description,
      AnnotationValueType.Select, Some(1), options).fold(
      err => throw new Error(err.list.mkString),
        annotationType => {
          domainObjects = domainObjects + (classOf[CollectionEventAnnotationType] -> annotationType)
          annotationType
        }
    )
  }

  def createCollectionEventTypeSpecimenGroupData: CollectionEventTypeSpecimenGroupData = {
    val sg = defaultSpecimenGroup
    val ceventTypeSpecimenGroup = CollectionEventTypeSpecimenGroupData(
      sg.id.id, 1, Some(BigDecimal(1.0)))
    domainObjects = domainObjects + (classOf[CollectionEventTypeSpecimenGroupData] -> ceventTypeSpecimenGroup)
    ceventTypeSpecimenGroup
  }

  def createCollectionEventTypeAnnotationTypeData: CollectionEventTypeAnnotationTypeData = {
    val annotationType = defaultCollectionEventAnnotationType
    val ceventTypeAnnotationType = CollectionEventTypeAnnotationTypeData(
      annotationType.id.id, true)
    domainObjects = domainObjects +
      (classOf[CollectionEventTypeAnnotationTypeData] -> ceventTypeAnnotationType)
    ceventTypeAnnotationType
  }

  def createParticipantAnnotationType: ParticipantAnnotationType = {
    val id = AnnotationTypeId(nameGenerator.next[ParticipantAnnotationType])
    val name = nameGenerator.next[ParticipantAnnotationType]
    val description = Some(nameGenerator.next[ParticipantAnnotationType])
    val options = Some(Seq(
      nameGenerator.next[String],
      nameGenerator.next[String]))

    val disabledStudy = defaultDisabledStudy
    ParticipantAnnotationType.create(
      disabledStudy.id, id, -1L, DateTime.now, name, description, AnnotationValueType.Select,
      Some(1), options, required = true).fold(
      err => throw new Error(err.list.mkString),
        annotationType => {
          domainObjects = domainObjects + (classOf[ParticipantAnnotationType] -> annotationType)
            annotationType
        }
    )
  }

  def createSpecimenLinkAnnotationType: SpecimenLinkAnnotationType = {
    val id = AnnotationTypeId(nameGenerator.next[SpecimenLinkAnnotationType])
    val name = nameGenerator.next[SpecimenLinkAnnotationType]
    val description = Some(nameGenerator.next[SpecimenLinkAnnotationType])
    val options = Some(Seq(
      nameGenerator.next[String],
      nameGenerator.next[String]))

    val disabledStudy = defaultDisabledStudy
    SpecimenLinkAnnotationType.create(
      disabledStudy.id, id, -1L, DateTime.now, name, description,
      AnnotationValueType.Select, Some(1), options).fold(
      err => throw new Error(err.list.mkString),
        annotationType => {
          domainObjects = domainObjects + (classOf[SpecimenLinkAnnotationType] -> annotationType)
          annotationType
        }
    )
  }

  def createProcessingType: ProcessingType = {
    val processingTypeId = ProcessingTypeId(nameGenerator.next[ProcessingType])
    val name = nameGenerator.next[ProcessingType]
    val description = Some(nameGenerator.next[ProcessingType])

    val disabledStudy = defaultDisabledStudy
    ProcessingType.create(
      disabledStudy.id, processingTypeId, -1L, DateTime.now, name, description, enabled = true).fold(
      err => throw new Error(err.list.mkString),
        processingType => {
          domainObjects = domainObjects + (classOf[ProcessingType] -> processingType)
          processingType
        }
    )
  }

  def createSpecimenLinkType: SpecimenLinkType = {
    val processingType = defaultProcessingType
    val id = SpecimenLinkTypeId(nameGenerator.next[SpecimenLinkType])
    val expectedInputChange = BigDecimal(1.0)
    val expectedOutpuChange = BigDecimal(1.0)
    val inputCount = 1
    val outputCount = 1

    val disabledStudy = defaultDisabledStudy

    SpecimenLinkType.create(
      processingType.id, id, -1L, DateTime.now, expectedInputChange,
      expectedOutpuChange, inputCount, outputCount,
      SpecimenGroupId(nameGenerator.next[SpecimenGroup]),
      SpecimenGroupId(nameGenerator.next[SpecimenGroup]),
      annotationTypeData = List.empty).fold(
      err => throw new Error(err.list.mkString),
        slt => {
          domainObjects = domainObjects + (classOf[SpecimenLinkType] -> slt)
          slt
        }
    )
  }

  def createSpecimenLinkTypeAndSpecimenGroups: (SpecimenLinkType, SpecimenGroup, SpecimenGroup) = {
    val inputSg = createSpecimenGroup
    val outputSg = createSpecimenGroup
    val slType = createSpecimenLinkType.copy(inputGroupId = inputSg.id, outputGroupId = outputSg.id)
    (slType, inputSg, outputSg)
  }

  def createSpecimenLinkTypeAnnotationTypeData: SpecimenLinkTypeAnnotationTypeData = {
    val annotationType = defaultSpecimenLinkAnnotationType
    val specimenLinkTypeAnnotationType = SpecimenLinkTypeAnnotationTypeData(
      annotationType.id.id, true)
    domainObjects = domainObjects +
    (classOf[SpecimenLinkTypeAnnotationTypeData] -> specimenLinkTypeAnnotationType)
    specimenLinkTypeAnnotationType
  }

  def createDisabledCentre: DisabledCentre = {
    val id = CentreId(nameGenerator.next[Centre])
    val name = nameGenerator.next[Centre]
    val description = Some(nameGenerator.next[Centre])

    DisabledCentre.create(id, -1L, DateTime.now, name, description).fold(
      err => throw new Error(err.list.mkString),
      centre => {
        domainObjects = domainObjects + (classOf[DisabledCentre] -> centre)
        centre
      }
    )
  }

  def createEnabledCentre: EnabledCentre = {
    val disabledCentre = defaultDisabledCentre
    val enabledCentre = disabledCentre.enable | null
    domainObjects = domainObjects + (classOf[EnabledCentre] -> enabledCentre)
    domainObjects = domainObjects - classOf[DisabledCentre]
    enabledCentre
  }

  def createLocation: Location = {
    Location(
      LocationId(nameGenerator.next[Location]),
      nameGenerator.next[Location],
      nameGenerator.next[Location],
      nameGenerator.next[Location],
      nameGenerator.next[Location],
      nameGenerator.next[Location],
      Some(nameGenerator.next[Location]),
      nameGenerator.next[Location])
  }

  def defaultRegisteredUser: RegisteredUser = {
    defaultObject(classOf[RegisteredUser], createRegisteredUser)
  }

  def defaultActiveUser: ActiveUser = {
    defaultObject(classOf[ActiveUser], createActiveUser)
  }

  def defaultLockedUser: LockedUser = {
    defaultObject(classOf[LockedUser], createLockedUser)
  }

  def defaultDisabledStudy: DisabledStudy = {
    defaultObject(classOf[DisabledStudy], createDisabledStudy)
  }

  def defaultEnabledStudy: EnabledStudy = {
    defaultObject(classOf[EnabledStudy], createEnabledStudy)
  }

  def defaultSpecimenGroup: SpecimenGroup = {
    defaultObject(classOf[SpecimenGroup], createSpecimenGroup)
  }

  def defaultCollectionEventType: CollectionEventType = {
    defaultObject(classOf[CollectionEventType], createCollectionEventType)
  }

  def defaultCollectionEventAnnotationType: CollectionEventAnnotationType = {
    defaultObject(classOf[CollectionEventAnnotationType], createCollectionEventAnnotationType)
  }

  def defaultCollectionEventTypeSpecimenGroupData: CollectionEventTypeSpecimenGroupData = {
    defaultObject(classOf[CollectionEventTypeSpecimenGroupData], createCollectionEventTypeSpecimenGroupData)
  }

  def defaultCollectionEventTypeAnnotationTypeData: CollectionEventTypeAnnotationTypeData = {
    defaultObject(
      classOf[CollectionEventTypeAnnotationTypeData],
      createCollectionEventTypeAnnotationTypeData)
  }

  def defaultParticipantAnnotationType: ParticipantAnnotationType = {
    defaultObject(
      classOf[ParticipantAnnotationType],
      createParticipantAnnotationType)
  }

  def defaultSpecimenLinkAnnotationType: SpecimenLinkAnnotationType = {
    defaultObject(
      classOf[SpecimenLinkAnnotationType],
      createSpecimenLinkAnnotationType)
  }

  def defaultProcessingType: ProcessingType = {
    defaultObject(classOf[ProcessingType], createProcessingType)
  }

  def defaultSpecimenLinkType: SpecimenLinkType = {
    defaultObject(classOf[SpecimenLinkType], createSpecimenLinkType)
  }

  def defaultSpecimenLinkTypeAnnotationTypeData: SpecimenLinkTypeAnnotationTypeData = {
    defaultObject(
      classOf[SpecimenLinkTypeAnnotationTypeData],
      createSpecimenLinkTypeAnnotationTypeData)
  }

  def defaultDisabledCentre: DisabledCentre = {
    defaultObject(classOf[DisabledCentre], createDisabledCentre)
  }

  def defaultEnabledCentre: EnabledCentre = {
    defaultObject(classOf[EnabledCentre], createEnabledCentre)
  }

  def defaultLocation: Location = {
    defaultObject(classOf[Location], createLocation)
  }

  /** Retrieves the class from the map, or calls 'create' if value does not exist
    */
  private def defaultObject[T](key: Class[T], create: => T): T = {
    domainObjects get key match {
      case Some(obj) => key.cast(obj)
      case None => create
    }
  }
}

