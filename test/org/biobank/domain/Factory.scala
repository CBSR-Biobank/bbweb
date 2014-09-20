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
    val user = RegisteredUser(
      version        = 0L,
      timeAdded      = DateTime.now,
      timeModified = None,
      name           = nameGenerator.next[User],
      email          = nameGenerator.nextEmail[User],
      id             = UserId(nameGenerator.next[User]),
      password       = nameGenerator.next[User],
      salt           = nameGenerator.next[User],
      avatarUrl      = Some(nameGenerator.nextUrl[User]))
    domainObjects = domainObjects + (classOf[RegisteredUser] -> user)
    user
  }

  def createActiveUser: ActiveUser = {
    val user = ActiveUser(
      version        = 0L,
      timeAdded      = DateTime.now,
      timeModified = None,
      name           = nameGenerator.next[User],
      email          = nameGenerator.nextEmail[User],
      id             = UserId(nameGenerator.next[User]),
      password       = nameGenerator.next[User],
      salt           = nameGenerator.next[User],
      avatarUrl      = Some(nameGenerator.nextUrl[User]))
    domainObjects = domainObjects + (classOf[ActiveUser] -> user)
    user
  }

  def createLockedUser: LockedUser = {
    val user = LockedUser(
      version        = 0L,
      timeAdded      = DateTime.now,
      timeModified = None,
      name           = nameGenerator.next[User],
      email          = nameGenerator.nextEmail[User],
      id             = UserId(nameGenerator.next[User]),
      password       = nameGenerator.next[User],
      salt           = nameGenerator.next[User],
      avatarUrl      = Some(nameGenerator.nextUrl[User]))
    domainObjects = domainObjects + (classOf[LockedUser] -> user)
    user
  }
  def createDisabledStudy: DisabledStudy = {
    val study = DisabledStudy(
      version        = 0L,
      timeAdded      = DateTime.now,
      timeModified = None,
      id             = StudyId(nameGenerator.next[Study]),
      name           = nameGenerator.next[Study],
      description    = Some(nameGenerator.next[Study]))
    domainObjects = domainObjects + (classOf[DisabledStudy] -> study)
    study
  }

  def createEnabledStudy: EnabledStudy = {
    val enabledStudy = EnabledStudy(
      id             = StudyId(nameGenerator.next[Study]),
      version        = 0L,
      timeAdded      = DateTime.now,
      timeModified = None,
      name           = nameGenerator.next[Study],
      description    = Some(nameGenerator.next[Study]))
    domainObjects = domainObjects + (classOf[EnabledStudy] -> enabledStudy)
    enabledStudy
  }

  def createRetiredStudy: RetiredStudy = {
    val retiredStudy = RetiredStudy(
      id             = StudyId(nameGenerator.next[Study]),
      version        = 0L,
      timeAdded      = DateTime.now,
      timeModified = None,
      name           = nameGenerator.next[Study],
      description    = Some(nameGenerator.next[Study]))
    domainObjects = domainObjects + (classOf[RetiredStudy] -> retiredStudy)
    retiredStudy
  }

  def createSpecimenGroup: SpecimenGroup = {
    val disabledStudy = defaultDisabledStudy
    val specimenGroup = SpecimenGroup(
      id                          = SpecimenGroupId(nameGenerator.next[SpecimenGroup]),
      studyId                     = disabledStudy.id,
      version                     = 0L,
      timeAdded                   = DateTime.now,
      timeModified              = None,
      name                        = nameGenerator.next[SpecimenGroup],
      description                 = Some(nameGenerator.next[SpecimenGroup]),
      units                       = nameGenerator.next[String],
      anatomicalSourceType        = AnatomicalSourceType.Blood,
      preservationType            = PreservationType.FreshSpecimen,
      preservationTemperatureType = PreservationTemperatureType.Minus80celcius,
      specimenType                = SpecimenType.FilteredUrine)
  domainObjects = domainObjects + (classOf[SpecimenGroup] -> specimenGroup)
  specimenGroup
  }

  def createCollectionEventType: CollectionEventType = {
    val disabledStudy = defaultDisabledStudy
    val ceventType = CollectionEventType(
      id                 = CollectionEventTypeId(nameGenerator.next[CollectionEventType]),
      studyId            = disabledStudy.id,
      version            = 0L,
      timeAdded          = DateTime.now,
      timeModified     = None,
      name               = nameGenerator.next[CollectionEventType],
      description        = Some(nameGenerator.next[CollectionEventType]),
      recurring          = false,
      specimenGroupData  = List.empty,
      annotationTypeData = List.empty)

    domainObjects = domainObjects + (classOf[CollectionEventType] -> ceventType)
    ceventType
  }

  def createCollectionEventAnnotationType: CollectionEventAnnotationType = {
    val disabledStudy = defaultDisabledStudy
    val annotationType = CollectionEventAnnotationType(
      id             = AnnotationTypeId(nameGenerator.next[CollectionEventAnnotationType]),
      studyId        = disabledStudy.id,
      version        = 0L,
      timeAdded      = DateTime.now,
      timeModified = None,
      name           = nameGenerator.next[CollectionEventAnnotationType],
      description    = Some(nameGenerator.next[CollectionEventAnnotationType]),
      valueType      = AnnotationValueType.Select,
      maxValueCount  = Some(1),
      options        = Some(Seq(nameGenerator.next[String], nameGenerator.next[String])))

    domainObjects = domainObjects + (classOf[CollectionEventAnnotationType] -> annotationType)
    annotationType
  }

  def createCollectionEventTypeSpecimenGroupData: CollectionEventTypeSpecimenGroupData = {
    val sg = defaultSpecimenGroup
    val ceventTypeSpecimenGroup = CollectionEventTypeSpecimenGroupData(
      specimenGroupId = sg.id.id,
      maxCount = 1,
      amount = Some(BigDecimal(1.0)))
    domainObjects = domainObjects + (classOf[CollectionEventTypeSpecimenGroupData] -> ceventTypeSpecimenGroup)
    ceventTypeSpecimenGroup
  }

  def createCollectionEventTypeAnnotationTypeData: CollectionEventTypeAnnotationTypeData = {
    val annotationType = defaultCollectionEventAnnotationType
    val ceventTypeAnnotationType = CollectionEventTypeAnnotationTypeData(
      annotationTypeId = annotationType.id.id, required = true)
    domainObjects = domainObjects +
      (classOf[CollectionEventTypeAnnotationTypeData] -> ceventTypeAnnotationType)
    ceventTypeAnnotationType
  }

  def createParticipantAnnotationType: ParticipantAnnotationType = {
    val disabledStudy = defaultDisabledStudy
    val annotationType = ParticipantAnnotationType(
      id             = AnnotationTypeId(nameGenerator.next[ParticipantAnnotationType]),
      studyId        = disabledStudy.id,
      version        = 0L,
      timeAdded      = DateTime.now,
      timeModified = None,
      name           = nameGenerator.next[ParticipantAnnotationType],
      description    = Some(nameGenerator.next[ParticipantAnnotationType]),
      valueType      = AnnotationValueType.Select,
      maxValueCount  = Some(1),
      options        = Some(Seq(nameGenerator.next[String], nameGenerator.next[String])),
      required       = true)
    domainObjects = domainObjects + (classOf[ParticipantAnnotationType] -> annotationType)
    annotationType
  }

  def createSpecimenLinkAnnotationType: SpecimenLinkAnnotationType = {
    val disabledStudy = defaultDisabledStudy
    val annotationType = SpecimenLinkAnnotationType(
      id             = AnnotationTypeId(nameGenerator.next[SpecimenLinkAnnotationType]),
      studyId        = disabledStudy.id,
      version        = 0L,
      timeAdded      = DateTime.now,
      timeModified = None,
      name           = nameGenerator.next[SpecimenLinkAnnotationType],
      description    = Some(nameGenerator.next[SpecimenLinkAnnotationType]),
      valueType      = AnnotationValueType.Select,
      maxValueCount  = Some(1),
      options        = Some(Seq(nameGenerator.next[String], nameGenerator.next[String])))
    domainObjects = domainObjects + (classOf[SpecimenLinkAnnotationType] -> annotationType)
    annotationType
  }

  def createProcessingType: ProcessingType = {
    val disabledStudy = defaultDisabledStudy
    val processingType = ProcessingType(
      id             = ProcessingTypeId(nameGenerator.next[ProcessingType]),
      studyId        = disabledStudy.id,
      version        = 0L,
      timeAdded      = DateTime.now,
      timeModified = None,
      name           = nameGenerator.next[ProcessingType],
      description    = Some(nameGenerator.next[ProcessingType]),
      enabled        = false)

    domainObjects = domainObjects + (classOf[ProcessingType] -> processingType)
    processingType
  }

  def createSpecimenLinkType: SpecimenLinkType = {
    val slt = SpecimenLinkType(
      id                    = SpecimenLinkTypeId(nameGenerator.next[SpecimenLinkType]),
      processingTypeId      = defaultProcessingType.id,
      version               = 0L,
      timeAdded             = DateTime.now,
      timeModified        = None,
      expectedInputChange   = BigDecimal(1.0),
      expectedOutputChange  = BigDecimal(1.0),
      inputCount            = 1,
      outputCount           = 1,
      inputGroupId          = SpecimenGroupId(nameGenerator.next[SpecimenLinkType]),
      outputGroupId         = SpecimenGroupId(nameGenerator.next[SpecimenLinkType]),
      inputContainerTypeId  = None,
      outputContainerTypeId = None,
      annotationTypeData    = List.empty)

    domainObjects = domainObjects + (classOf[SpecimenLinkType] -> slt)
    slt
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
      annotationType.id.id, required = true)
    domainObjects = domainObjects +
    (classOf[SpecimenLinkTypeAnnotationTypeData] -> specimenLinkTypeAnnotationType)
    specimenLinkTypeAnnotationType
  }

  def createDisabledCentre: DisabledCentre = {
    val centre = DisabledCentre(
      id             = CentreId(nameGenerator.next[Centre]),
      version        = 0L,
      timeAdded      = DateTime.now,
      timeModified = None,
      name           = nameGenerator.next[Centre],
      description    = Some(nameGenerator.next[Centre]))

    domainObjects = domainObjects + (classOf[DisabledCentre] -> centre)
    centre
  }

  def createEnabledCentre: EnabledCentre = {
    val centre = EnabledCentre(
      id             = CentreId(nameGenerator.next[Centre]),
      version        = 0L,
      timeAdded      = DateTime.now,
      timeModified = None,
      name           = nameGenerator.next[Centre],
      description    = Some(nameGenerator.next[Centre]))
    domainObjects = domainObjects + (classOf[EnabledCentre] -> centre)
    centre
  }

  def createLocation: Location = {
    val location = Location(
      LocationId(nameGenerator.next[Location]),
      nameGenerator.next[Location],
      nameGenerator.next[Location],
      nameGenerator.next[Location],
      nameGenerator.next[Location],
      nameGenerator.next[Location],
      Some(nameGenerator.next[Location]),
      nameGenerator.next[Location])
    domainObjects = domainObjects + (classOf[Location] -> location)
    location
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

