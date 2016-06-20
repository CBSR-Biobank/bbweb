package org.biobank.domain

import org.biobank.fixture.NameGenerator
import org.biobank.domain._
import org.biobank.domain.user._
import org.biobank.domain.study._
import org.biobank.domain.participants._
import org.biobank.domain.centre._
import org.biobank.domain.containers._
import org.biobank.domain.AnnotationValueType._
import org.slf4j.LoggerFactory
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
                              description  = Some(nameGenerator.next[Study]),
                              annotationTypes = Set.empty)
    domainObjects = domainObjects + (classOf[DisabledStudy] -> study)
    study
  }

  def createEnabledStudy(): EnabledStudy = {
    val enabledStudy = EnabledStudy(id           = StudyId(nameGenerator.next[Study]),
                                    version      = 0L,
                                    timeAdded    = DateTime.now,
                                    timeModified = None,
                                    name         = nameGenerator.next[Study],
                                    description  = Some(nameGenerator.next[Study]),
                                    annotationTypes = Set.empty)
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
        description    = Some(nameGenerator.next[Study]),
        annotationTypes = Set.empty)
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

  def createCollectionSpecimenSpec(): CollectionSpecimenSpec = {
    val specimenSpec = CollectionSpecimenSpec(
        uniqueId                    = nameGenerator.next[CollectionSpecimenSpec],
        name                        = nameGenerator.next[CollectionSpecimenSpec],
        description                 = Some(nameGenerator.next[CollectionSpecimenSpec]),
        units                       = nameGenerator.next[String],
        anatomicalSourceType        = AnatomicalSourceType.Blood,
        preservationType            = PreservationType.FreshSpecimen,
        preservationTemperatureType = PreservationTemperatureType.Minus80celcius,
        specimenType                = SpecimenType.FilteredUrine,
        maxCount                    = 1,
        amount                      = Some(BigDecimal(0.5)))
    domainObjects = domainObjects + (classOf[CollectionSpecimenSpec] -> specimenSpec)
    specimenSpec
  }

  def createCollectionEventType(): CollectionEventType = {
    val disabledStudy = defaultDisabledStudy
    val ceventType = CollectionEventType(
        id              = CollectionEventTypeId(nameGenerator.next[CollectionEventType]),
        studyId         = disabledStudy.id,
        version         = 0L,
        timeAdded       = DateTime.now,
        timeModified    = None,
        name            = nameGenerator.next[CollectionEventType],
        description     = Some(nameGenerator.next[CollectionEventType]),
        recurring       = false,
        specimenSpecs   = Set.empty,
        annotationTypes = Set.empty)

    domainObjects = domainObjects + (classOf[CollectionEventType] -> ceventType)
    ceventType
  }

  def createAnnotationType(uniqueId:      String,
                           valueType:     AnnotationValueType,
                           maxValueCount: Int,
                           options:       Seq[String]) = {
    val (vtMaxValueCount, vtOptions) = valueType match {
        case AnnotationValueType.Text     => (None, Seq.empty)
        case AnnotationValueType.Number   => (None, Seq.empty)
        case AnnotationValueType.DateTime => (None, Seq.empty)
        case AnnotationValueType.Select   => (Some(maxValueCount), options)
      }

    (defaultDisabledStudy.id,
     id,
     nameGenerator.next[AnnotationType],
     Some(nameGenerator.next[AnnotationType]),
     valueType,
     vtMaxValueCount,
     vtOptions,
     false)
  }

  def createAnnotationType(valueType:     AnnotationValueType,
                           maxValueCount: Option[Int],
                           options:       Seq[String]): AnnotationType = {
    val annotationType = AnnotationType(nameGenerator.next[AnnotationType],
                                        nameGenerator.next[AnnotationType],
                                        None,
                                        valueType,
                                        maxValueCount,
                                        options,
                                        false)

    domainObjects = domainObjects + (classOf[AnnotationType] -> annotationType)
    annotationType
  }

  def createAnnotationType(valueType: AnnotationValueType): AnnotationType = {
    createAnnotationType(valueType, None, Seq.empty)
  }

  def createAnnotationType(): AnnotationType = {
    createAnnotationType(AnnotationValueType.Text, None, Seq.empty)
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

  def createAnnotationValues(annotationType: AnnotationType):
      Tuple3[Option[String], Option[String], Set[String]] = {
    annotationType.valueType match {
      case Text     =>
        (Some(nameGenerator.next[Annotation]), None, Set.empty)
      case Number   =>
        (None, Some(scala.util.Random.nextFloat.toString), Set.empty)
      case AnnotationValueType.DateTime =>
        (Some(ISODateTimeFormat.dateTime.print(DateTime.now)), None, Set.empty)
      case Select   =>
        (None, None, Set(annotationType.options(0)))
    }
  }

  def createAnnotation(annotationType: AnnotationType): Annotation = {
    val (stringValue, numberValue, selectedValues) = createAnnotationValues(annotationType)
    val annot = Annotation(annotationTypeId = defaultAnnotationType.uniqueId,
                           stringValue            = stringValue,
                           numberValue            = numberValue,
                           selectedValues         = selectedValues)
    domainObjects = domainObjects + (classOf[Annotation] -> annot)
    annot
  }

  def createAnnotation(): Annotation = {
    createAnnotation(defaultAnnotationType)
  }

  def createCollectionEvent(): CollectionEvent = {
    val participant = defaultParticipant
    val collectionEventType = defaultCollectionEventType

    val cevent = CollectionEvent(
        id                    = CollectionEventId(nameGenerator.next[CollectionEvent]),
        participantId         = participant.id,
        collectionEventTypeId = collectionEventType.id,
        version               = 0,
        timeAdded             = DateTime.now,
        timeModified          = None,
        timeCompleted         = DateTime.now,
        visitNumber           = 1,
        annotations           = Set.empty)
    domainObjects = domainObjects + (classOf[CollectionEvent] -> cevent)
    cevent
  }

  def createUsableSpecimen(): UsableSpecimen = {
    val specimenSpec = defaultCollectionSpecimenSpec
    val location = defaultLocation

    val specimen = UsableSpecimen(
        id               = SpecimenId(nameGenerator.next[Specimen]),
        inventoryId      = nameGenerator.next[Specimen],
        specimenSpecId   = specimenSpec.uniqueId,
        version          = 0,
        timeAdded        = DateTime.now,
        timeModified     = None,
        originLocationId = location.uniqueId,
        locationId       = location.uniqueId,
        containerId      = None,
        positionId       = None,
        timeCreated      = DateTime.now,
        amount           = BigDecimal(1.0)
      )
    domainObjects = domainObjects + (classOf[Specimen] -> specimen)
    specimen
  }

  def createUnusableSpecimen(): UnusableSpecimen = {
    val specimenSpec = defaultCollectionSpecimenSpec
    val location = defaultLocation

    val specimen = UnusableSpecimen(
        id               = SpecimenId(nameGenerator.next[Specimen]),
        inventoryId      = nameGenerator.next[Specimen],
        specimenSpecId   = specimenSpec.uniqueId,
        version          = 0,
        timeAdded        = DateTime.now,
        timeModified     = None,
        originLocationId = location.uniqueId,
        locationId       = location.uniqueId,
        containerId      = None,
        positionId       = None,
        timeCreated      = DateTime.now,
        amount           = BigDecimal(1.0)
      )
    domainObjects = domainObjects + (classOf[Specimen] -> specimen)
    specimen
  }

  def createDisabledCentre(): DisabledCentre = {
    val centre = DisabledCentre(id           = CentreId(nameGenerator.next[Centre]),
                                version      = 0L,
                                timeAdded    = DateTime.now,
                                timeModified = None,
                                name         = nameGenerator.next[Centre],
                                description  = Some(nameGenerator.next[Centre]),
                                studyIds     = Set.empty,
                                locations    = Set.empty)

    domainObjects = domainObjects + (classOf[DisabledCentre] -> centre)
    centre
  }

  def createEnabledCentre(): EnabledCentre = {
    val centre = EnabledCentre(id           = CentreId(nameGenerator.next[Centre]),
                               version      = 0L,
                               timeAdded    = DateTime.now,
                               timeModified = None,
                               name         = nameGenerator.next[Centre],
                               description  = Some(nameGenerator.next[Centre]),
                               studyIds     = Set.empty,
                               locations    = Set.empty)
    domainObjects = domainObjects + (classOf[EnabledCentre] -> centre)
    centre
  }

  def createLocation(): Location = {
    val location = Location(uniqueId       = nameGenerator.next[Location],
                            name           = nameGenerator.next[Location],
                            street         = nameGenerator.next[Location],
                            city           = nameGenerator.next[Location],
                            province       = nameGenerator.next[Location],
                            postalCode     = nameGenerator.next[Location],
                            poBoxNumber    = Some(nameGenerator.next[Location]),
                            countryIsoCode = nameGenerator.next[Location])
    domainObjects = domainObjects + (classOf[Location] -> location)
    location
  }

  def createContainerSchema(): ContainerSchema = {
    val containerSchema = ContainerSchema(
        version      = 0L,
        timeAdded    = DateTime.now,
        timeModified = None,
        id           = ContainerSchemaId(nameGenerator.next[ContainerSchema]),
        name         = nameGenerator.next[ContainerSchema],
        description  = Some(nameGenerator.next[ContainerSchema]),
        shared       = true)
    domainObjects = domainObjects + (classOf[ContainerSchema] -> containerSchema)
    containerSchema
  }

  def createShipment(): Shipment = {
    val location = defaultLocation

    val shipment = Shipment(id             = ShipmentId(nameGenerator.next[Shipment]),
                            version        = 0L,
                            timeAdded      = DateTime.now,
                            timeModified   = None,
                            state          = ShipmentState.Created,
                            courierName    = nameGenerator.next[Shipment],
                            trackingNumber = nameGenerator.next[Shipment],
                            fromLocationId = location.uniqueId,
                            toLocationId   = location.uniqueId,
                            timePacked     = None,
                            timeSent       = None,
                            timeReceived   = None,
                            timeUnpacked   = None)
    domainObjects = domainObjects + (classOf[Shipment] -> shipment)
    shipment
  }

  def createPackedShipment(): Shipment = {
    createShipment.copy(state = ShipmentState.Packed,
                        timePacked = Some(DateTime.now.minusDays(10)))
  }

  def createSentShipment(): Shipment = {
    val shipment = createPackedShipment
    shipment.copy(state = ShipmentState.Sent,
                  timeSent = Some(shipment.timePacked.get.plusDays(1)))
  }

  def createReceivedShipment(): Shipment = {
    val shipment = createSentShipment
    shipment.copy(state = ShipmentState.Received,
                  timeReceived = Some(shipment.timeSent.get.plusDays(1)))
  }

  def createUnpackedShipment(): Shipment = {
    val shipment = createReceivedShipment
    shipment.copy(state = ShipmentState.Unpacked,
                  timeUnpacked = Some(shipment.timeReceived.get.plusDays(1)))
  }

  def createLostShipment(): Shipment = {
    createSentShipment.copy(state = ShipmentState.Lost)
  }

  def createShipmentSpecimen(): ShipmentSpecimen = {
    val specimen = defaultUsableSpecimen
    val shipment = defaultShipment

    val shipmentSpecimen = ShipmentSpecimen(
        id                  = ShipmentSpecimenId(nameGenerator.next[ShipmentSpecimen]),
        version             = 0L,
        timeAdded           = DateTime.now,
        timeModified        = None,
        shipmentId          = shipment.id,
        specimenId          = specimen.id,
        state               = ShipmentItemState.Present,
        shipmentContainerId = None)
    domainObjects = domainObjects + (classOf[ShipmentSpecimen] -> shipmentSpecimen)
    shipmentSpecimen
  }

  def createShipmentContainer(): ShipmentContainer = {
    ???
    // val container = defaultContainer
    // val shipment = defaultShipment

    // val shipmentContainer = ShipmentContainer(
    //     id                  = ShipmentContainerId(nameGenerator.next[ShipmentContainer]),
    //     version             = 0L,
    //     timeAdded           = DateTime.now,
    //     timeModified        = None,
    //     shipmentId          = shipment.id,
    //     containerId         = container.id,
    //     state               = ShipmentItemState.Present)
    // domainObjects = domainObjects + (classOf[ShipmentContainer] -> shipmentContainer)
    // shipmentContainer
  }

  // def createEnabledContainerType(centre: Centre): EnabledContainerType = {
  //   val containerType = EnabledContainerType(
  //     id           = ContainerTypeId(nameGenerator.next[ContainerType]),
  //     centreId     = Some(centre.id),
  //     schemaId     = defaultContainerSchema.id,
  //     version      = 0L,
  //     timeAdded    = DateTime.now,
  //     timeModified = None,
  //     name         = nameGenerator.next[ContainerType],
  //     description  = Some(nameGenerator.next[ContainerType]),
  //     shared       = true)
  //   domainObjects = domainObjects + (classOf[EnabledContainerType] -> containerType)
  //   containerType
  // }

  // def createEnabledContainerType(): EnabledContainerType = {
  //   createEnabledContainerType(defaultEnabledCentre)
  // }

  // def createDisabledContainerType(): DisabledContainerType = {
  //   val containerType = DisabledContainerType(
  //     version      = 0L,
  //     centreId     = Some(defaultEnabledCentre.id),
  //     schemaId     = defaultContainerSchema.id,
  //     timeAdded    = DateTime.now,
  //     timeModified = None,
  //     id           = ContainerTypeId(nameGenerator.next[ContainerType]),
  //     name         = nameGenerator.next[ContainerType],
  //     description  = Some(nameGenerator.next[ContainerType]),
  //     shared       = true)
  //   domainObjects = domainObjects + (classOf[DisabledContainerType] -> containerType)
  //   containerType
  // }

  // def defaultRegisteredUser: RegisteredUser = {
  //   defaultObject(classOf[RegisteredUser], createRegisteredUser)
  // }

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

  def defaultCollectionSpecimenSpec: CollectionSpecimenSpec = {
    defaultObject(classOf[CollectionSpecimenSpec], createCollectionSpecimenSpec)
  }

  def defaultAnnotationType: AnnotationType = {
    defaultObject(classOf[AnnotationType], createAnnotationType)
  }

  def defaultProcessingType: ProcessingType = {
    defaultObject(classOf[ProcessingType], createProcessingType)
  }

  def defaultSpecimenLinkType: SpecimenLinkType = {
    defaultObject(classOf[SpecimenLinkType], createSpecimenLinkType)
  }

  def defaultParticipant: Participant = {
    defaultObject(classOf[Participant], createParticipant)
  }

  def defaultAnnotation: Annotation = {
    defaultObject(classOf[Annotation], createAnnotation)
  }

  def defaultCollectionEvent(): CollectionEvent = {
    defaultObject(classOf[CollectionEvent], createCollectionEvent)
  }

  def defaultUsableSpecimen: UsableSpecimen = {
    defaultObject(classOf[UsableSpecimen], createUsableSpecimen)
  }

  def defaultUnusableSpecimen: UnusableSpecimen = {
    defaultObject(classOf[UnusableSpecimen], createUnusableSpecimen)
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

  def defaultContainerSchema: ContainerSchema = {
    defaultObject(classOf[ContainerSchema], createContainerSchema)
  }

  def defaultShipment: Shipment = {
    defaultObject(classOf[Shipment], createShipment)
  }

  def defaultShipmentSpecimen: ShipmentSpecimen = {
    defaultObject(classOf[ShipmentSpecimen], createShipmentSpecimen)
  }

  def defaultShipmentContainer: ShipmentContainer = {
    defaultObject(classOf[ShipmentContainer], createShipmentContainer)
  }

  // def defaultDisabledContainerType: DisabledContainerType = {
  //   defaultObject(classOf[DisabledContainerType], createDisabledContainerType)
  // }

  // def defaultEnabledContainerType: EnabledContainerType = {
  //   defaultObject(classOf[EnabledContainerType], createEnabledContainerType)
  // }

  /** Retrieves the class from the map, or calls 'create' if value does not exist
   */
  private def defaultObject[T](key: Class[T], create: => T): T = {
    domainObjects.get(key).fold { create } { obj => key.cast(obj) }
  }
}
