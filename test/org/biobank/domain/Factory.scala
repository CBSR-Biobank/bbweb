package org.biobank.domain

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import org.biobank.fixture.NameGenerator
import org.biobank.domain._
import org.biobank.domain.user._
import org.biobank.domain.access._
import org.biobank.domain.study._
import org.biobank.domain.participants._
import org.biobank.domain.centre._
import org.biobank.domain.containers._
import org.biobank.domain.AnnotationValueType._
import org.slf4j.LoggerFactory
import scalaz.Scalaz._

/**
 * This factory class creates domain entities that can be used in test cases.
 *
 * The factory remembers the previously created domain etities. Entities of each type are cached, but only the
 * last one created.
 *
 * If an entity has a dependency on another, the other is created first, or if the other entity has
 * already been created it will be used.  For example, if a participant is created, it will belong to the last
 * study that was created.x
 *
 */
class Factory {

  val log = LoggerFactory.getLogger(this.getClass)

  val nameGenerator = new NameGenerator(this.getClass)

  var domainObjects: Map[Class[_], _] = Map.empty

  def createRegisteredUser(): RegisteredUser = {
    val name = nameGenerator.next[User]
    val user = RegisteredUser(version      = 0L,
                              timeAdded    = OffsetDateTime.now,
                              timeModified = None,
                              slug         = Slug(name),
                              name         = name,
                              email        = nameGenerator.nextEmail[User],
                              id           = UserId(nameGenerator.next[User]),
                              password     = nameGenerator.next[User],
                              salt         = nameGenerator.next[User],
                              avatarUrl    = Some(nameGenerator.nextUrl[User]))
    domainObjects  = domainObjects + (classOf[RegisteredUser] -> user)
    user
  }

  def createActiveUser: ActiveUser = {
    val name = nameGenerator.next[User]
    val user = ActiveUser(version      = 0L,
                          timeAdded    = OffsetDateTime.now,
                          timeModified = None,
                          slug         = Slug(name),
                          name         = name,
                          email        = nameGenerator.nextEmail[User],
                          id           = UserId(nameGenerator.next[User]),
                          password     = nameGenerator.next[User],
                          salt         = nameGenerator.next[User],
                          avatarUrl    = Some(nameGenerator.nextUrl[User]))
    domainObjects = domainObjects + (classOf[ActiveUser] -> user)
    user
  }

  def createLockedUser(): LockedUser = {
    val name = nameGenerator.next[User]
    val user = LockedUser(version      = 0L,
                          timeAdded    = OffsetDateTime.now,
                          timeModified = None,
                          slug         = Slug(name),
                          name         = name,
                          email        = nameGenerator.nextEmail[User],
                          id           = UserId(nameGenerator.next[User]),
                          password     = nameGenerator.next[User],
                          salt         = nameGenerator.next[User],
                          avatarUrl    = Some(nameGenerator.nextUrl[User]))
    domainObjects = domainObjects + (classOf[LockedUser] -> user)
    user
  }

  def createRole(): Role = {
    val name = nameGenerator.next[Role]
    val role = Role(id           = AccessItemId(nameGenerator.next[AccessItem]),
                    version      = 0L,
                    timeAdded    = OffsetDateTime.now,
                    timeModified = None,
                    slug         = Slug(name),
                    name         = name,
                    description  = Some(nameGenerator.next[Role]),
                    userIds      = Set.empty[UserId],
                    parentIds    = Set.empty[AccessItemId],
                    childrenIds  = Set.empty[AccessItemId])
    domainObjects = domainObjects + (classOf[Role] -> role)
    role
  }

  def createPermission(): Permission = {
    val name = nameGenerator.next[Permission]
    val permission = Permission(id           = AccessItemId(nameGenerator.next[AccessItem]),
                                version      = 0L,
                                timeAdded    = OffsetDateTime.now,
                                timeModified = None,
                                slug         = Slug(name),
                                name         = name,
                                description  = Some(nameGenerator.next[Permission]),
                                parentIds    = Set.empty[AccessItemId],
                                childrenIds  = Set.empty[AccessItemId])
    domainObjects = domainObjects + (classOf[Permission] -> permission)
    permission
  }

  def createMembership(): Membership = {
    val name = nameGenerator.next[Membership]
    val membership = Membership(id           = MembershipId(nameGenerator.next[MembershipId]),
                                version      = 0L,
                                timeAdded    = OffsetDateTime.now,
                                timeModified = None,
                                slug         = Slug(name),
                                name         = name,
                                description  = Some(nameGenerator.next[Membership]),
                                userIds      = Set.empty[UserId],
                                studyData    = MembershipEntitySet(false, Set.empty[StudyId]),
                                centreData   = MembershipEntitySet(false, Set.empty[CentreId]))
    domainObjects = domainObjects + (classOf[Membership] -> membership)
    membership
  }

  def createDisabledStudy(): DisabledStudy = {
    val name = nameGenerator.next[Study]
    val study = DisabledStudy(version         = 0L,
                              timeAdded       = OffsetDateTime.now,
                              timeModified    = None,
                              id              = StudyId(nameGenerator.next[Study]),
                              slug            = Slug(name),
                              name            = name,
                              description     = Some(nameGenerator.next[Study]),
                              annotationTypes = Set.empty)
    domainObjects = domainObjects + (classOf[DisabledStudy] -> study)
    study
  }

  def createEnabledStudy(): EnabledStudy = {
    val name = nameGenerator.next[Study]
    val enabledStudy = EnabledStudy(id              = StudyId(nameGenerator.next[Study]),
                                    version         = 0L,
                                    timeAdded       = OffsetDateTime.now,
                                    timeModified    = None,
                                    slug            = Slug(name),
                                    name            = name,
                                    description     = Some(nameGenerator.next[Study]),
                                    annotationTypes = Set.empty)
    domainObjects = domainObjects + (classOf[EnabledStudy] -> enabledStudy)
    enabledStudy
  }

  def createRetiredStudy(): RetiredStudy = {
    val name = nameGenerator.next[Study]
    val retiredStudy = RetiredStudy(id              = StudyId(nameGenerator.next[Study]),
                                    version         = 0L,
                                    timeAdded       = OffsetDateTime.now,
                                    timeModified    = None,
                                    slug            = Slug(name),
                                    name            = name,
                                    description     = Some(nameGenerator.next[Study]),
                                    annotationTypes = Set.empty)
    domainObjects = domainObjects + (classOf[RetiredStudy] -> retiredStudy)
    retiredStudy
  }

  def createSpecimenGroup(): SpecimenGroup = {
    val disabledStudy = defaultDisabledStudy
    val name = nameGenerator.next[SpecimenGroup]
    val specimenGroup = SpecimenGroup(
        id                          = SpecimenGroupId(nameGenerator.next[SpecimenGroup]),
        studyId                     = disabledStudy.id,
        version                     = 0L,
        timeAdded                   = OffsetDateTime.now,
        timeModified                = None,
        slug                        = Slug(name),
        name                        = name,
        description                 = Some(nameGenerator.next[SpecimenGroup]),
        units                       = nameGenerator.next[String],
        anatomicalSourceType        = AnatomicalSourceType.Blood,
        preservationType            = PreservationType.FreshSpecimen,
        preservationTemperatureType = PreservationTemperatureType.Minus80celcius,
        specimenType                = SpecimenType.FilteredUrine)
    domainObjects = domainObjects + (classOf[SpecimenGroup] -> specimenGroup)
    specimenGroup
  }

  def createCollectionSpecimenDescription(): CollectionSpecimenDescription = {
    val name = nameGenerator.next[CollectionSpecimenDescription]
    val specimenSpec = CollectionSpecimenDescription(
        id                          = SpecimenDescriptionId(nameGenerator.next[CollectionSpecimenDescription]),
        slug                        = Slug(name),
        name                        = name,
        description                 = Some(nameGenerator.next[CollectionSpecimenDescription]),
        units                       = nameGenerator.next[String],
        anatomicalSourceType        = AnatomicalSourceType.Blood,
        preservationType            = PreservationType.FreshSpecimen,
        preservationTemperatureType = PreservationTemperatureType.Minus80celcius,
        specimenType                = SpecimenType.FilteredUrine,
        maxCount                    = 1,
        amount                      = BigDecimal(0.5))
    domainObjects = domainObjects + (classOf[CollectionSpecimenDescription] -> specimenSpec)
    specimenSpec
  }

  def createCollectionEventType(): CollectionEventType = {
    val disabledStudy = defaultDisabledStudy
    val name = nameGenerator.next[CollectionEventType]
    val ceventType = CollectionEventType(
        id                   = CollectionEventTypeId(nameGenerator.next[CollectionEventType]),
        studyId              = disabledStudy.id,
        version              = 0L,
        timeAdded            = OffsetDateTime.now,
        timeModified         = None,
        slug                 = Slug(name),
        name                 = name,
        description          = Some(nameGenerator.next[CollectionEventType]),
        recurring            = false,
        specimenDescriptions = Set.empty,
        annotationTypes      = Set.empty)

    domainObjects = domainObjects + (classOf[CollectionEventType] -> ceventType)
    ceventType
  }

  def createAnnotationType(valueType:     AnnotationValueType,
                           maxValueCount: Option[Int],
                           options:       Seq[String]): AnnotationType = {
    val name = nameGenerator.next[AnnotationType]
    val annotationType = AnnotationType(AnnotationTypeId(nameGenerator.next[AnnotationType]),
                                        Slug(name),
                                        name,
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
    val name = nameGenerator.next[ProcessingType]
    val processingType = ProcessingType(
        id             = ProcessingTypeId(nameGenerator.next[ProcessingType]),
        studyId        = disabledStudy.id,
        version        = 0L,
        timeAdded      = OffsetDateTime.now,
        timeModified   = None,
        slug           = Slug(name),
        name           = name,
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
        timeAdded             = OffsetDateTime.now,
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
    val uniqueId = nameGenerator.next[Participant]
    val participant = Participant(
        studyId      = study.id,
        id           = ParticipantId(nameGenerator.next[Participant]),
        version      = 0L,
        timeAdded    = OffsetDateTime.now,
        timeModified = None,
        slug         = Slug(uniqueId),
        uniqueId     = uniqueId,
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
        (Some(OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)), None, Set.empty)
      case Select   =>
        (None, None, Set(annotationType.options(0)))
    }
  }

  def createAnnotation(): Annotation = {
    val annot = Annotation(annotationTypeId = defaultAnnotationType.id,
                           stringValue      = None,
                           numberValue      = None,
                           selectedValues   = Set.empty[String])
    domainObjects = domainObjects + (classOf[Annotation] -> annot)
    annot
  }

  def createAnnotationWithValues(annotationType: AnnotationType): Annotation = {
    val (stringValue, numberValue, selectedValues) = createAnnotationValues(annotationType)
    val annot = createAnnotation.copy(stringValue      = stringValue,
                                      numberValue      = numberValue,
                                      selectedValues   = selectedValues)
    domainObjects = domainObjects + (classOf[Annotation] -> annot)
    annot
  }

  def createCollectionEvent(): CollectionEvent = {
    val participant = defaultParticipant
    val collectionEventType = defaultCollectionEventType

    val cevent = CollectionEvent(
        id                    = CollectionEventId(nameGenerator.next[CollectionEvent]),
        participantId         = participant.id,
        collectionEventTypeId = collectionEventType.id,
        version               = 0,
        timeAdded             = OffsetDateTime.now,
        timeModified          = None,
        timeCompleted         = OffsetDateTime.now,
        visitNumber           = 1,
        annotations           = Set.empty)
    domainObjects = domainObjects + (classOf[CollectionEvent] -> cevent)
    cevent
  }

  def createUsableSpecimen(): UsableSpecimen = {
    val specimenDescription = defaultCollectionSpecimenDescription
    val location            = defaultLocation
    val inventoryId         = nameGenerator.next[Specimen]

    val specimen = UsableSpecimen(
        id                    = SpecimenId(nameGenerator.next[Specimen]),
        version               = 0,
        timeAdded             = OffsetDateTime.now,
        timeModified          = None,
        slug                  = Slug(inventoryId),
        inventoryId           = inventoryId,
        specimenDescriptionId = specimenDescription.id,
        originLocationId      = location.id,
        locationId            = location.id,
        containerId           = None,
        positionId            = None,
        timeCreated           = OffsetDateTime.now,
        amount                = BigDecimal(1.0)
      )
    domainObjects = domainObjects + (classOf[UsableSpecimen] -> specimen)
    specimen
  }

  def createUnusableSpecimen(): UnusableSpecimen = {
    val specimenDescription = defaultCollectionSpecimenDescription
    val location            = defaultLocation
    val inventoryId         = nameGenerator.next[Specimen]

    val specimen = UnusableSpecimen(
        id                    = SpecimenId(nameGenerator.next[Specimen]),
        version               = 0,
        timeAdded             = OffsetDateTime.now,
        timeModified          = None,
        slug                  = Slug(inventoryId),
        inventoryId           = inventoryId,
        specimenDescriptionId = specimenDescription.id,
        originLocationId      = location.id,
        locationId            = location.id,
        containerId           = None,
        positionId            = None,
        timeCreated           = OffsetDateTime.now,
        amount                = BigDecimal(1.0)
      )
    domainObjects = domainObjects + (classOf[UnusableSpecimen] -> specimen)
    specimen
  }

  def createDisabledCentre(): DisabledCentre = {
    val name = nameGenerator.next[Centre]
    val centre = DisabledCentre(id           = CentreId(nameGenerator.next[Centre]),
                                version      = 0L,
                                timeAdded    = OffsetDateTime.now,
                                timeModified = None,
                                slug         = Slug(name),
                                name         = name,
                                description  = Some(nameGenerator.next[Centre]),
                                studyIds     = Set.empty,
                                locations    = Set.empty)

    domainObjects = domainObjects + (classOf[DisabledCentre] -> centre)
    centre
  }

  def createEnabledCentre(): EnabledCentre = {
    val name = nameGenerator.next[Centre]
    val centre = EnabledCentre(id           = CentreId(nameGenerator.next[Centre]),
                               version      = 0L,
                               timeAdded    = OffsetDateTime.now,
                               timeModified = None,
                               slug         = Slug(name),
                               name         = name,
                               description  = Some(nameGenerator.next[Centre]),
                               studyIds     = Set.empty,
                               locations    = Set.empty)
    domainObjects = domainObjects + (classOf[EnabledCentre] -> centre)
    centre
  }

  def createLocation(): Location = {
    val name = nameGenerator.next[Location]
    val location = Location(id             = LocationId(nameGenerator.next[Location]),
                            slug           = Slug(name),
                            name           = name,
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
    val name = nameGenerator.next[ContainerSchema]
    val containerSchema = ContainerSchema(
        version      = 0L,
        timeAdded    = OffsetDateTime.now,
        timeModified = None,
        id           = ContainerSchemaId(nameGenerator.next[ContainerSchema]),
        slug         = Slug(name),
        name         = name,
        description  = Some(nameGenerator.next[ContainerSchema]),
        shared       = true)
    domainObjects = domainObjects + (classOf[ContainerSchema] -> containerSchema)
    containerSchema
  }

  def createShipment(fromCentre:   Centre,
                     fromLocation: Location,
                     toCentre:     Centre,
                     toLocation:   Location): CreatedShipment = {
    val shipment = CreatedShipment(id             = ShipmentId(nameGenerator.next[Shipment]),
                                   version        = 0L,
                                   timeAdded      = OffsetDateTime.now,
                                   timeModified   = None,
                                   courierName    = nameGenerator.next[Shipment],
                                   trackingNumber = nameGenerator.next[Shipment],
                                   fromCentreId   = fromCentre.id,
                                   fromLocationId = fromLocation.id,
                                   toCentreId     = toCentre.id,
                                   toLocationId   = toLocation.id,
                                   timePacked     = None,
                                   timeSent       = None,
                                   timeReceived   = None,
                                   timeUnpacked   = None,
                                   timeCompleted  = None)
    domainObjects = domainObjects + (classOf[Shipment] -> shipment)
    shipment
  }

  /**
   * Assumes fromCentre and toCentre have at least one location and use the first locations.
   */
  def createShipment(fromCentre: Centre, toCentre: Centre): CreatedShipment = {
    createShipment(fromCentre, fromCentre.locations.head, toCentre, toCentre.locations.head)
  }

  def createShipment: CreatedShipment = {
    val centre = defaultEnabledCentre
    val location = defaultLocation
    createShipment(centre, location, centre, location)
  }

  def createPackedShipment(fromCentre: Centre, toCentre: Centre): PackedShipment = {
    createShipment(fromCentre, toCentre).pack(OffsetDateTime.now.minusDays(10))
  }

  def createSentShipment(fromCentre: Centre, toCentre: Centre): SentShipment = {
    val shipment = createPackedShipment(fromCentre, toCentre)
    shipment.send(shipment.timePacked.get.plusDays(1)).fold(
      err => sys.error("failed to create a sent shipment"),
      s   => s
    )
  }

  def createReceivedShipment(fromCentre: Centre, toCentre: Centre): ReceivedShipment = {
    val shipment = createSentShipment(fromCentre, toCentre)
    shipment.receive(shipment.timeSent.get.plusDays(1)).fold(
      err => sys.error("failed to create a received shipment"),
      s   => s
    )
  }

  def createUnpackedShipment(fromCentre: Centre, toCentre: Centre): UnpackedShipment = {
    val shipment = createReceivedShipment(fromCentre, toCentre)
    shipment.unpack(shipment.timeReceived.get.plusDays(1)).fold(
      err => sys.error("failed to create a unpacked shipment"),
      s   => s
    )
  }

  def createCompletedShipment(fromCentre: Centre, toCentre: Centre): CompletedShipment = {
    val shipment = createUnpackedShipment(fromCentre, toCentre)
    shipment.complete(shipment.timeReceived.get.plusDays(1)).fold(
      err => sys.error("failed to create a completed shipment"),
      s   => s
    )
  }

  def createLostShipment(fromCentre: Centre, toCentre: Centre): LostShipment = {
    createSentShipment(fromCentre, toCentre).lost
  }

  def createShipmentSpecimen(): ShipmentSpecimen = {
    val specimen = defaultUsableSpecimen
    val shipment = defaultShipment

    val shipmentSpecimen = ShipmentSpecimen(
        id                  = ShipmentSpecimenId(nameGenerator.next[ShipmentSpecimen]),
        version             = 0L,
        timeAdded           = OffsetDateTime.now,
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
    //     timeAdded           = OffsetDateTime.now,
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
  //     timeAdded    = OffsetDateTime.now,
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
  //     timeAdded    = OffsetDateTime.now,
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

  def defaultCollectionSpecimenDescription: CollectionSpecimenDescription = {
    defaultObject(classOf[CollectionSpecimenDescription], createCollectionSpecimenDescription)
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
