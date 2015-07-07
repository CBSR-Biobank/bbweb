package org.biobank.domain

import org.biobank.fixture.NameGenerator
import org.biobank.domain._
import org.biobank.domain.user._
import org.biobank.domain.study._
import org.biobank.domain.participants._
import org.biobank.domain.centre._
import org.biobank.infrastructure.{
  CollectionEventTypeAnnotationTypeData,
  CollectionEventTypeSpecimenGroupData,
  SpecimenLinkTypeAnnotationTypeData
}
import org.biobank.domain.AnnotationValueType._
import org.slf4j.LoggerFactory
import scala.reflect.ClassTag
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import scalaz.Scalaz._
import com.github.nscala_time.time.Imports._

/**
 * This factory class creates domain entities that can be used in test cases.
 *
 * The factory remembers the previously created domain etities. Entities of each type are cached, but only the
 * last one created.
 *
 * If an entity has a dependency on another, the other is created first, or if the other entity has
 * already been created it will be used.  For example, if a participant is created, it will belong to the last
 * study that was created.
 *
 */
class Factory {

  val log = LoggerFactory.getLogger(this.getClass)

  val nameGenerator = new NameGenerator(this.getClass)

  var domainObjects: Map[Class[_], _] = Map.empty

  def createRegisteredUser(): RegisteredUser = {
    val user = RegisteredUser(version      = 0L,
                              timeAdded    = DateTime.now,
                              timeModified = None,
                              name         = nameGenerator.next[User],
                              email        = nameGenerator.nextEmail[User],
                              id           = UserId(nameGenerator.next[User]),
                              password     = nameGenerator.next[User],
                              salt         = nameGenerator.next[User],
                              avatarUrl    = Some(nameGenerator.nextUrl[User]))
    domainObjects  = domainObjects + (classOf[RegisteredUser] -> user)
    user
  }

  def createActiveUser: ActiveUser = {
    val user = ActiveUser(version      = 0L,
                          timeAdded    = DateTime.now,
                          timeModified = None,
                          name         = nameGenerator.next[User],
                          email        = nameGenerator.nextEmail[User],
                          id           = UserId(nameGenerator.next[User]),
                          password     = nameGenerator.next[User],
                          salt         = nameGenerator.next[User],
                          avatarUrl    = Some(nameGenerator.nextUrl[User]))
    domainObjects = domainObjects + (classOf[ActiveUser] -> user)
    user
  }

  def createLockedUser(): LockedUser = {
    val user = LockedUser(version      = 0L,
                          timeAdded    = DateTime.now,
                          timeModified = None,
                          name         = nameGenerator.next[User],
                          email        = nameGenerator.nextEmail[User],
                          id           = UserId(nameGenerator.next[User]),
                          password     = nameGenerator.next[User],
                          salt         = nameGenerator.next[User],
                          avatarUrl    = Some(nameGenerator.nextUrl[User]))
    domainObjects  = domainObjects + (classOf[LockedUser] -> user)
    user
  }

  def createDisabledStudy(): DisabledStudy = {
    val study = DisabledStudy(version      = 0L,
                              timeAdded    = DateTime.now,
                              timeModified = None,
                              id           = StudyId(nameGenerator.next[Study]),
                              name         = nameGenerator.next[Study],
                              description  = Some(nameGenerator.next[Study]))
    domainObjects = domainObjects + (classOf[DisabledStudy] -> study)
    study
  }

  def createEnabledStudy(): EnabledStudy = {
    val enabledStudy = EnabledStudy(id           = StudyId(nameGenerator.next[Study]),
                                    version      = 0L,
                                    timeAdded    = DateTime.now,
                                    timeModified = None,
                                    name         = nameGenerator.next[Study],
                                    description  = Some(nameGenerator.next[Study]))
    domainObjects = domainObjects + (classOf[EnabledStudy] -> enabledStudy)
    enabledStudy
  }

  def createRetiredStudy(): RetiredStudy = {
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

  def createSpecimenGroup(): SpecimenGroup = {
    val disabledStudy = defaultDisabledStudy
    val specimenGroup = SpecimenGroup(
      id                          = SpecimenGroupId(nameGenerator.next[SpecimenGroup]),
      studyId                     = disabledStudy.id,
      version                     = 0L,
      timeAdded                   = DateTime.now,
      timeModified                = None,
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

  def createCollectionEventType(): CollectionEventType = {
    val disabledStudy = defaultDisabledStudy
    val ceventType = CollectionEventType(
      id                 = CollectionEventTypeId(nameGenerator.next[CollectionEventType]),
      studyId            = disabledStudy.id,
      version            = 0L,
      timeAdded          = DateTime.now,
      timeModified       = None,
      name               = nameGenerator.next[CollectionEventType],
      description        = Some(nameGenerator.next[CollectionEventType]),
      recurring          = false,
      specimenGroupData  = List.empty,
      annotationTypeData = List.empty)

    domainObjects = domainObjects + (classOf[CollectionEventType] -> ceventType)
    ceventType
  }

  def createAnnotationType(valueType:     AnnotationValueType,
                           maxValueCount: Int,
                           options:       Seq[String]) = {
    val (vtMaxValueCount, vtOptions) = valueType match {
      case AnnotationValueType.Text     => (None, Seq.empty)
      case AnnotationValueType.Number   => (None, Seq.empty)
      case AnnotationValueType.DateTime => (None, Seq.empty)
      case AnnotationValueType.Select   => (Some(maxValueCount), options)
    }

    (defaultDisabledStudy.id,
     0L,
     DateTime.now,
     None,
     nameGenerator.next[AnnotationType],
     Some(nameGenerator.next[AnnotationType]),
     vtMaxValueCount,
     vtOptions)
  }


  def createCollectionEventAnnotationType(valueType:     AnnotationValueType,
                                          maxValueCount: Int,
                                          options:       Seq[String])
      : CollectionEventAnnotationType = {
    val (studyId,
         version,
         timeAdded,
         timeModified,
         name,
         description,
         vtMaxValueCount,
         vtOptions) = createAnnotationType(valueType, maxValueCount, options)

    val annotationType = CollectionEventAnnotationType(
      id            = AnnotationTypeId(nameGenerator.next[CollectionEventAnnotationType]),
      studyId       = studyId,
      version       = version,
      timeAdded     = timeAdded,
      timeModified  = timeModified,
      name          = name,
      description   = description,
      valueType     = valueType,
      maxValueCount = vtMaxValueCount,
      options       = vtOptions)

    domainObjects = domainObjects + (classOf[CollectionEventAnnotationType] -> annotationType)
    annotationType
  }

  def createCollectionEventAnnotationType(): CollectionEventAnnotationType = {
    createCollectionEventAnnotationType(AnnotationValueType.Text, 0, Seq.empty)
  }

  def createCollectionEventTypeSpecimenGroupData(): CollectionEventTypeSpecimenGroupData = {
    val sg = defaultSpecimenGroup
    val ceventTypeSpecimenGroup = CollectionEventTypeSpecimenGroupData(
      specimenGroupId = sg.id.id,
      maxCount        = 1,
      amount          = Some(BigDecimal(1.0)))
    domainObjects = domainObjects + (classOf[CollectionEventTypeSpecimenGroupData] -> ceventTypeSpecimenGroup)
    ceventTypeSpecimenGroup
  }

  def createCollectionEventTypeAnnotationTypeData(): CollectionEventTypeAnnotationTypeData = {
    val annotationType = defaultCollectionEventAnnotationType
    val ceventTypeAnnotationType = CollectionEventTypeAnnotationTypeData(
      annotationTypeId = annotationType.id.id, required = true)
    domainObjects = domainObjects +
      (classOf[CollectionEventTypeAnnotationTypeData] -> ceventTypeAnnotationType)
    ceventTypeAnnotationType
  }

  def createParticipantAnnotationType(valueType:     AnnotationValueType,
                                      maxValueCount: Int,
                                      options:       Seq[String],
                                      required:      Boolean)
      : ParticipantAnnotationType = {
    val (studyId,
         version,
         timeAdded,
         timeModified,
         name,
         description,
         vtMaxValueCount,
         vtOptions) = createAnnotationType(valueType, maxValueCount, options)

    val annotationType = ParticipantAnnotationType(
      id            = AnnotationTypeId(nameGenerator.next[ParticipantAnnotationType]),
      studyId       = studyId,
      version       = version,
      timeAdded     = timeAdded,
      timeModified  = timeModified,
      name          = name,
      description   = description,
      valueType     = valueType,
      maxValueCount = vtMaxValueCount,
      options       = vtOptions,
      required      = required)
    domainObjects = domainObjects + (classOf[ParticipantAnnotationType] -> annotationType)
    annotationType
  }

  def createParticipantAnnotationType(): ParticipantAnnotationType = {
    createParticipantAnnotationType(AnnotationValueType.Text, 0, Seq.empty, false)
  }

  def createSpecimenLinkAnnotationType(valueType:     AnnotationValueType,
                                       maxValueCount: Int,
                                       options:       Seq[String])
      : SpecimenLinkAnnotationType = {
    val (studyId,
         version,
         timeAdded,
         timeModified,
         name,
         description,
         vtMaxValueCount,
         vtOptions) = createAnnotationType(valueType, maxValueCount, options)

    val annotationType = SpecimenLinkAnnotationType(
      id             = AnnotationTypeId(nameGenerator.next[SpecimenLinkAnnotationType]),
      studyId        = studyId,
      version        = version,
      timeAdded      = timeAdded,
      timeModified   = timeModified,
      name           = name,
      description    = description,
      valueType      = valueType,
      maxValueCount  = vtMaxValueCount,
      options        = vtOptions)
    domainObjects = domainObjects + (classOf[SpecimenLinkAnnotationType] -> annotationType)
    annotationType
  }

  def createSpecimenLinkAnnotationType(): SpecimenLinkAnnotationType = {
    createSpecimenLinkAnnotationType(AnnotationValueType.Text, 0, Seq.empty)
  }

  def createProcessingType(): ProcessingType = {
    val disabledStudy = defaultDisabledStudy
    val processingType = ProcessingType(
      id             = ProcessingTypeId(nameGenerator.next[ProcessingType]),
      studyId        = disabledStudy.id,
      version        = 0L,
      timeAdded      = DateTime.now,
      timeModified   = None,
      name           = nameGenerator.next[ProcessingType],
      description    = Some(nameGenerator.next[ProcessingType]),
      enabled        = false)

    domainObjects = domainObjects + (classOf[ProcessingType] -> processingType)
    processingType
  }

  def createSpecimenLinkType(): SpecimenLinkType = {
    val slt = SpecimenLinkType(
      id                    = SpecimenLinkTypeId(nameGenerator.next[SpecimenLinkType]),
      processingTypeId      = defaultProcessingType.id,
      version               = 0L,
      timeAdded             = DateTime.now,
      timeModified          = None,
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

  def createSpecimenLinkTypeAndSpecimenGroups(): (SpecimenLinkType, SpecimenGroup, SpecimenGroup) = {
    val inputSg = createSpecimenGroup
    val outputSg = createSpecimenGroup
    val slType = createSpecimenLinkType.copy(inputGroupId = inputSg.id, outputGroupId = outputSg.id)
    (slType, inputSg, outputSg)
  }

  def createSpecimenLinkTypeAnnotationTypeData(): SpecimenLinkTypeAnnotationTypeData = {
    val annotationType = defaultSpecimenLinkAnnotationType
    val specimenLinkTypeAnnotationType = SpecimenLinkTypeAnnotationTypeData(
      annotationType.id.id, required = true)
    domainObjects = domainObjects +
    (classOf[SpecimenLinkTypeAnnotationTypeData] -> specimenLinkTypeAnnotationType)
    specimenLinkTypeAnnotationType
  }

  def createParticipant(): Participant = {
    val study = defaultEnabledStudy
    val participant = Participant(
      studyId      = study.id,
      id           = ParticipantId(nameGenerator.next[Participant]),
      version      = 0L,
      timeAdded    = DateTime.now,
      timeModified = None,
      uniqueId     = nameGenerator.next[Participant],
      annotations  = Set.empty
    )
    domainObjects = domainObjects + (classOf[Participant] -> participant)
    participant
 }

  def createAnnotationValues[T <: StudyAnnotationType](annotationType: T):
      Tuple3[Option[String], Option[String], List[AnnotationOption]] = {
    annotationType.valueType match {
      case Text     =>
        (Some(nameGenerator.next[Annotation[_]]), None, List.empty)
      case Number   =>
        (None, Some(scala.util.Random.nextFloat.toString), List.empty)
      case AnnotationValueType.DateTime =>
        (Some(ISODateTimeFormat.dateTime.print(DateTime.now)), None, List.empty)
      case Select   =>
        (None, None, List(AnnotationOption(annotationType.id, annotationType.options(0))))
    }
  }

  def createParticipantAnnotation(): ParticipantAnnotation = {
    val annotationType = defaultParticipantAnnotationType
    val (stringValue, numberValue, selectedValues) = createAnnotationValues(annotationType)
    val annot = ParticipantAnnotation(annotationTypeId = annotationType.id,
                                      stringValue      = stringValue,
                                      numberValue      = numberValue,
                                      selectedValues   = selectedValues)
    domainObjects = domainObjects + (classOf[ParticipantAnnotation] -> annot)
    annot
  }

  def createCollectionEventAnnotation(annotationType: CollectionEventAnnotationType)
      : CollectionEventAnnotation = {
    val (stringValue, numberValue, selectedValues) = createAnnotationValues(annotationType)
    val annot = CollectionEventAnnotation(annotationTypeId = annotationType.id,
                                          stringValue      = stringValue,
                                          numberValue      = numberValue,
                                          selectedValues   = selectedValues)
    domainObjects = domainObjects + (classOf[CollectionEventAnnotation] -> annot)
    annot
  }

  def createCollectionEventAnnotation(): CollectionEventAnnotation = {
    createCollectionEventAnnotation(defaultCollectionEventAnnotationType)
  }

  def createCollectionEvent(): CollectionEvent = {
    val participant = defaultParticipant
    val collectionEventType = defaultCollectionEventType

    val cevent = CollectionEvent(
      id                    = CollectionEventId(nameGenerator.next[CollectionEvent]),
      participantId         = ParticipantId(participant.id.id),
      collectionEventTypeId = CollectionEventTypeId(collectionEventType.id.id),
      version               = 0,
      timeAdded             = DateTime.now,
      timeModified          = None,
      timeCompleted         = DateTime.now,
      visitNumber           = 1,
      annotations           = Set.empty)
    domainObjects = domainObjects + (classOf[CollectionEvent] -> cevent)
    cevent
  }

  def createDisabledCentre(): DisabledCentre = {
    val centre = DisabledCentre(id           = CentreId(nameGenerator.next[Centre]),
                                version      = 0L,
                                timeAdded    = DateTime.now,
                                timeModified = None,
                                name         = nameGenerator.next[Centre],
                                description  = Some(nameGenerator.next[Centre]))

    domainObjects = domainObjects + (classOf[DisabledCentre] -> centre)
    centre
  }

  def createEnabledCentre(): EnabledCentre = {
    val centre = EnabledCentre(id           = CentreId(nameGenerator.next[Centre]),
                               version      = 0L,
                               timeAdded    = DateTime.now,
                               timeModified = None,
                               name         = nameGenerator.next[Centre],
                               description  = Some(nameGenerator.next[Centre]))
    domainObjects = domainObjects + (classOf[EnabledCentre] -> centre)
    centre
  }

  def createLocation(): Location = {
    val location = Location(LocationId(nameGenerator.next[Location]),
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

  def defaultParticipantAnnotationType(annotationType: ParticipantAnnotationType) = {
    defaultObject(classOf[ParticipantAnnotationType], annotationType)
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

  def defaultParticipant: Participant = {
    defaultObject(classOf[Participant], createParticipant)
  }

  def defaultParticipantAnnotation: ParticipantAnnotation = {
    defaultObject(classOf[ParticipantAnnotation], createParticipantAnnotation)
  }

  def defaultCollectionEventAnnotation: CollectionEventAnnotation = {
    defaultObject(classOf[CollectionEventAnnotation], createCollectionEventAnnotation)
  }

  def defaultCollectionEvent(): CollectionEvent = {
    defaultObject(classOf[CollectionEvent], createCollectionEvent)
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
    domainObjects.get(key).fold { create } { obj => key.cast(obj) }
  }
}

