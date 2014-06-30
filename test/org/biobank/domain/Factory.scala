package org.biobank.domain

import org.biobank.fixture.NameGenerator
import org.biobank.domain._
import org.biobank.domain.study._
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

trait FactoryComponent {
  self: RepositoryComponent =>

  val factory: Factory = new Factory

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
      val hasher = nameGenerator.next[User]
      val salt = Some(nameGenerator.next[User])
      val avatarUrl = Some("http://test.com/")

      val validation = RegisteredUser.create(
        id, version, DateTime.now, name, email, password, hasher, salt, avatarUrl)
      if (validation.isFailure) {
        throw new Error
      }

      val user = validation | null
      domainObjects = domainObjects + (classOf[RegisteredUser] -> user)
      user
    }

    def createActiveUser: ActiveUser = {
      val registeredUser = defaultRegisteredUser
      val validation = registeredUser.activate(registeredUser.versionOption, DateTime.now)
      if (validation.isFailure) {
        throw new Error
      }

      val user = validation | null
      domainObjects = domainObjects + (classOf[ActiveUser] -> user)
      user
    }

    def createDisabledStudy: DisabledStudy = {
      val id = studyRepository.nextIdentity
      val name = nameGenerator.next[Study]
      val description = Some(nameGenerator.next[Study])

      val validation = DisabledStudy.create(id, -1L, DateTime.now, name, description)
      if (validation.isFailure) {
        throw new Error
      }

      val study = validation | null
      domainObjects = domainObjects + (classOf[DisabledStudy] -> study)
      study
    }

    def createEnabledStudy: EnabledStudy = {
      val disabledStudy = defaultDisabledStudy
      val enabledStudy = disabledStudy.enable(
        disabledStudy.versionOption, DateTime.now, 1, 1) | null
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
      val validation = SpecimenGroup.create(disabledStudy.id, sgId, -1L, DateTime.now,
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
      val validation = CollectionEventType.create(
        disabledStudy.id, ceventTypeId, -1L, DateTime.now, name,
        description, true, List.empty, List.empty)
      if (validation.isFailure) {
        throw new Error
      }

      val ceventType = validation | null
      domainObjects = domainObjects + (classOf[CollectionEventType] -> ceventType)
      ceventType
    }

    def createCollectionEventAnnotationType: CollectionEventAnnotationType = {
      val id = collectionEventAnnotationTypeRepository.nextIdentity
      val name = nameGenerator.next[CollectionEventAnnotationType]
      val description = Some(nameGenerator.next[CollectionEventAnnotationType])
      val options = Some(Seq(
        nameGenerator.next[String],
        nameGenerator.next[String]))

      val disabledStudy = defaultDisabledStudy
      val validation = CollectionEventAnnotationType.create(
        disabledStudy.id, id, -1L, DateTime.now, name, description,
        AnnotationValueType.Select, Some(1), options)
      if (validation.isFailure) {
        throw new Error
      }

      val annotationType = validation | null
      domainObjects = domainObjects + (classOf[CollectionEventAnnotationType] -> annotationType)
      annotationType
    }

    def createCollectionEventTypeSpecimenGroupData: CollectionEventTypeSpecimenGroupData = {
      val sg = defaultSpecimenGroup
      val ceventTypeSpecimenGroup = CollectionEventTypeSpecimenGroupData(sg.id.id, 1, Some(BigDecimal(1.0)))
      domainObjects = domainObjects + (classOf[CollectionEventTypeSpecimenGroupData] -> ceventTypeSpecimenGroup)
      ceventTypeSpecimenGroup
    }

    def createCollectionEventTypeAnnotationTypeData: CollectionEventTypeAnnotationTypeData = {
      val annotationType = defaultCollectionEventAnnotationType
      val ceventTypeAnnotationType = CollectionEventTypeAnnotationTypeData(annotationType.id.id, true)
      domainObjects = domainObjects +
      (classOf[CollectionEventTypeAnnotationTypeData] -> ceventTypeAnnotationType)
      ceventTypeAnnotationType
    }

    def createParticipantAnnotationType: ParticipantAnnotationType = {
      val id = participantAnnotationTypeRepository.nextIdentity
      val name = nameGenerator.next[ParticipantAnnotationType]
      val description = Some(nameGenerator.next[ParticipantAnnotationType])
      val options = Some(Seq(
        nameGenerator.next[String],
        nameGenerator.next[String]))

      val disabledStudy = defaultDisabledStudy
      val validation = ParticipantAnnotationType.create(
        disabledStudy.id, id, -1L, DateTime.now, name,
        description, AnnotationValueType.Select, Some(1), options, required = true)
      if (validation.isFailure) {
        throw new Error
      }

      val annotationType = validation | null
      domainObjects = domainObjects + (classOf[ParticipantAnnotationType] -> annotationType)
      annotationType
    }

    def createSpecimenLinkAnnotationType: SpecimenLinkAnnotationType = {
      val id = specimenLinkAnnotationTypeRepository.nextIdentity
      val name = nameGenerator.next[SpecimenLinkAnnotationType]
      val description = Some(nameGenerator.next[SpecimenLinkAnnotationType])
      val options = Some(Seq(
        nameGenerator.next[String],
        nameGenerator.next[String]))

      val disabledStudy = defaultDisabledStudy
      val validation = SpecimenLinkAnnotationType.create(
        disabledStudy.id, id, -1L, DateTime.now, name, description,
        AnnotationValueType.Select, Some(1), options)
      if (validation.isFailure) {
        throw new Error
      }

      val annotationType = validation | null
      domainObjects = domainObjects + (classOf[SpecimenLinkAnnotationType] -> annotationType)
      annotationType
    }

    def createProcessingType: ProcessingType = {
      val processingTypeId = processingTypeRepository.nextIdentity
      val name = nameGenerator.next[ProcessingType]
      val description = Some(nameGenerator.next[ProcessingType])

      val disabledStudy = defaultDisabledStudy
      val validation = ProcessingType.create(
        disabledStudy.id, processingTypeId, -1L, DateTime.now, name, description, enabled = true)
      if (validation.isFailure) {
              throw new Error
      }

      val processingType = validation | null
      domainObjects = domainObjects + (classOf[ProcessingType] -> processingType)
      processingType
    }

    def createSpecimenLinkType: SpecimenLinkType = {
      val processingType = defaultProcessingType
      val id = specimenLinkTypeRepository.nextIdentity
      val expectedInputChange = BigDecimal(1.0)
      val expectedOutpuChange = BigDecimal(1.0)
      val inputCount = 1
      val outputCount = 1

      val disabledStudy = defaultDisabledStudy

      val validation = SpecimenLinkType.create(
        processingType.id, id, -1L, DateTime.now, expectedInputChange,
        expectedOutpuChange, inputCount, outputCount,
        specimenGroupRepository.nextIdentity,
        specimenGroupRepository.nextIdentity,
        annotationTypeData = List.empty)

      if (validation.isFailure) {
        throw new Error
      }

      val annotationType = validation | null
      domainObjects = domainObjects + (classOf[SpecimenLinkType] -> annotationType)
      annotationType
    }

    def createSpecimenLinkTypeAndSpecimenGroups: (SpecimenLinkType, SpecimenGroup, SpecimenGroup) = {
      val inputSg = factory.createSpecimenGroup
      val outputSg = factory.createSpecimenGroup
      val slType = createSpecimenLinkType.copy(inputGroupId = inputSg.id, outputGroupId = outputSg.id)
      (slType, inputSg, outputSg)
    }

    def createSpecimenLinkTypeAnnotationTypeData: SpecimenLinkTypeAnnotationTypeData = {
      val annotationType = defaultSpecimenLinkAnnotationType
      val specimenLinkTypeAnnotationType = SpecimenLinkTypeAnnotationTypeData(annotationType.id.id, true)
      domainObjects = domainObjects +
      (classOf[SpecimenLinkTypeAnnotationTypeData] -> specimenLinkTypeAnnotationType)
      specimenLinkTypeAnnotationType
    }

    def defaultRegisteredUser: RegisteredUser = {
      defaultObject(classOf[RegisteredUser], createRegisteredUser)
    }

    def defaultActivUser: ActiveUser = {
      defaultObject(classOf[ActiveUser], createActiveUser)
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

    /** Retrieves the class from the map, or calls 'create' if value does not exist
      */
    private def defaultObject[T](key: Class[T], create: => T): T = {
      domainObjects get key match {
        case Some(obj) => key.cast(obj)
        case None => create
      }
    }

  }

}
