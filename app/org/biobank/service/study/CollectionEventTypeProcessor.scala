package org.biobank.service.study

import org.biobank.domain._
import org.biobank.domain.user.UserId
import org.biobank.domain.study.{
  Study,
  StudyId,
  CollectionEventType,
  CollectionEventTypeId,
  CollectionEventTypeRepository,
  CollectionEventAnnotationTypeRepository,
  SpecimenGroupId,
  SpecimenGroupRepository
}
import org.slf4j.LoggerFactory
import org.biobank.service.{ Processor, WrappedCommand, WrappedEvent }
import org.biobank.infrastructure._
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.infrastructure.event.StudyEventsUtil._
import org.biobank.infrastructure.event.StudyEvents._

import akka.persistence.SnapshotOffer
import org.joda.time.DateTime
import scaldi.akka.AkkaInjectable
import scaldi.{Injectable, Injector}
import scalaz._
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

/**
  * The CollectionEventTypeProcessor is responsible for maintaining state changes for all
  * [[org.biobank.domain.study.CollectionEventType]] aggregates. This particular processor uses
  * Akka-Persistence's [[akka.persistence.PersistentActor]]. It receives Commands and if valid will persist
  * the generated events, afterwhich it will updated the current state of the
  * [[org.biobank.domain.study.CollectionEventType]] being processed.
  *
  * It is a child actor of [[org.biobank.service.study.StudiesProcessorComponent.StudiesProcessor]].
  */
class CollectionEventTypeProcessor(implicit inj: Injector) extends Processor with AkkaInjectable {

  override def persistenceId = "collection-event-processor-id"

  case class SnapshotState(collectionEventTypes: Set[CollectionEventType])

  val collectionEventTypeRepository = inject [CollectionEventTypeRepository]

  val collectionEventAnnotationTypeRepository = inject [CollectionEventAnnotationTypeRepository]

  val specimenGroupRepository = inject [SpecimenGroupRepository]

  /**
    * These are the events that are recovered during journal recovery. They cannot fail and must be
    * processed to recreate the current state of the aggregate.
    */
  val receiveRecover: Receive = {
    case wevent: WrappedEvent[_] =>
      wevent.event match {
        case event: CollectionEventTypeAddedEvent =>   recoverCollectionEventTypeAddedEvent(event, wevent.userId, wevent.dateTime)
        case event: CollectionEventTypeUpdatedEvent => recoverCollectionEventTypeUpdatedEvent(event, wevent.userId, wevent.dateTime)
        case event: CollectionEventTypeRemovedEvent => recoverCollectionEventTypeRemovedEvent(event, wevent.userId, wevent.dateTime)

        case event => log.error(s"event not handled: $event")
      }

    case SnapshotOffer(_, snapshot: SnapshotState) =>
      snapshot.collectionEventTypes.foreach{ ceType =>
        collectionEventTypeRepository.put(ceType) }
  }

  /**
    * These are the commands that are requested. A command can fail, and will send the failure as a response
    * back to the user. Each valid command generates one or more events and is journaled.
    */
  val receiveCommand: Receive = {
    case procCmd: WrappedCommand =>
      implicit val userId = procCmd.userId
      procCmd.command match {
        case cmd: AddCollectionEventTypeCmd =>    processAddCollectionEventTypeCmd(cmd)
        case cmd: UpdateCollectionEventTypeCmd => processUpdateCollectionEventTypeCmd(cmd)
        case cmd: RemoveCollectionEventTypeCmd => processRemoveCollectionEventTypeCmd(cmd)
      }

    case "snap" =>
      saveSnapshot(SnapshotState(collectionEventTypeRepository.getValues.toSet))
      stash()

    case cmd => log.error(s"CollectionEventTypeProcessor: message not handled: $cmd")
  }

  def update
    (cmd: CollectionEventTypeModifyCommand)
    (fn: CollectionEventType => DomainValidation[CollectionEventType])
      : DomainValidation[CollectionEventType] = {
    for {
      cet <- collectionEventTypeRepository.withId(StudyId(cmd.studyId), CollectionEventTypeId(cmd.id))
      validVersion <-  cet.requireVersion(cmd.expectedVersion)
      updatedCet   <- fn(cet)
    } yield updatedCet
  }

  private def processAddCollectionEventTypeCmd
    (cmd: AddCollectionEventTypeCmd)
    (implicit userId: Option[UserId])
      : Unit = {
    val timeNow = DateTime.now
    val studyId = StudyId(cmd.studyId)
    val id = collectionEventTypeRepository.nextIdentity

    val event = for {
      nameValid <- nameAvailable(cmd.name)
      newItem <- CollectionEventType.create(
        studyId, id, -1L, timeNow, cmd.name, cmd.description, cmd.recurring,
        cmd.specimenGroupData, cmd.annotationTypeData)
      validSgData <- validateSpecimenGroupData(studyId, cmd.specimenGroupData)
      validAtData <- validateAnnotationTypeData(studyId, cmd.annotationTypeData)
      event <- CollectionEventTypeAddedEvent(
        studyId               = cmd.studyId,
        collectionEventTypeId = id.id,
        name                  = Some(newItem.name),
        description           = newItem.description,
        recurring             = Some(newItem.recurring),
        specimenGroupData     = convertSpecimenGroupDataToEvent(newItem.specimenGroupData),
        annotationTypeData    = convertAnnotationTypeDataToEvent(newItem.annotationTypeData)).success
    } yield event

    process(event){ wevent =>
      recoverCollectionEventTypeAddedEvent(wevent.event, wevent.userId, wevent.dateTime)
    }
  }

  private def processUpdateCollectionEventTypeCmd
    (cmd: UpdateCollectionEventTypeCmd)
    (implicit userId: Option[UserId])
      : Unit = {
    val timeNow = DateTime.now
    val studyId = StudyId(cmd.studyId)
    val v = update(cmd) { cet =>
      for {
        nameAvailable <- nameAvailable(cmd.name, CollectionEventTypeId(cmd.id))
        validSgData <- validateSpecimenGroupData(studyId, cmd.specimenGroupData)
        validAtData <- validateAnnotationTypeData(studyId, cmd.annotationTypeData)
        newItem <- cet.update(
          cmd.name, cmd.description, cmd.recurring, cmd.specimenGroupData, cmd.annotationTypeData)
      } yield newItem
    }

    val event = v.fold(
      err => DomainError(s"error $err occurred on $cmd").failureNel,
      cet => CollectionEventTypeUpdatedEvent(
        studyId               = cmd.studyId,
        collectionEventTypeId = cet.id.id,
        version               = Some(cet.version),
        name                  = Some(cet.name),
        description           = cet.description,
        recurring             = Some(cet.recurring),
        specimenGroupData     = convertSpecimenGroupDataToEvent(cet.specimenGroupData),
        annotationTypeData    = convertAnnotationTypeDataToEvent(cet.annotationTypeData)).success
    )

    process(event){ wevent =>
      recoverCollectionEventTypeUpdatedEvent(wevent.event, wevent.userId, wevent.dateTime)
    }
  }

  private def processRemoveCollectionEventTypeCmd
    (cmd: RemoveCollectionEventTypeCmd)
    (implicit userId: Option[UserId])
      : Unit = {
    val v = update(cmd) { at => at.success }

    val event = v.fold(
      err => DomainError(s"error $err occurred on $cmd").failureNel,
      cet =>  CollectionEventTypeRemovedEvent(cet.studyId.id, cet.id.id).success
    )

    process(event){ wevent =>
      recoverCollectionEventTypeRemovedEvent(wevent.event, wevent.userId, wevent.dateTime)
    }
  }

  private def recoverCollectionEventTypeAddedEvent
    (event: CollectionEventTypeAddedEvent, userId: Option[UserId], dateTime: DateTime)
      : Unit = {
    collectionEventTypeRepository.put(CollectionEventType(
      studyId            = StudyId(event.studyId),
      id                 = CollectionEventTypeId(event.collectionEventTypeId),
      version            = 0L,
      timeAdded          = dateTime,
      timeModified       = None,
      name               = event.getName,
      description        = event.description,
      recurring          = event.getRecurring,
      specimenGroupData  = convertSpecimenGroupDataFromEvent(event.specimenGroupData),
      annotationTypeData = convertCollectionEventTypeAnnotationTypeDataFromEvent(event.annotationTypeData)
    ))
    ()
  }

  private def recoverCollectionEventTypeUpdatedEvent
    (event: CollectionEventTypeUpdatedEvent, userId: Option[UserId], dateTime: DateTime)
      : Unit = {
    collectionEventTypeRepository.getByKey(CollectionEventTypeId(event.collectionEventTypeId)).fold(
      err => log.error(s"updating collection event type from event failed: $err"),
      cet => {
        collectionEventTypeRepository.put(cet.copy(
          version            = event.getVersion,
          timeModified       = Some(dateTime),
          name               = event.getName,
          description        = event.description,
          recurring          = event.getRecurring,
          specimenGroupData  = convertSpecimenGroupDataFromEvent(event.specimenGroupData),
          annotationTypeData = convertCollectionEventTypeAnnotationTypeDataFromEvent(event.annotationTypeData)
        ))
        ()
      }
    )
  }

  private def recoverCollectionEventTypeRemovedEvent
    (event: CollectionEventTypeRemovedEvent, userId: Option[UserId], dateTime: DateTime)
      : Unit = {
    collectionEventTypeRepository.getByKey(CollectionEventTypeId(event.collectionEventTypeId)).fold(
      err => log.error(s"updating collection event type from event failed: $err"),
      cet => {
        collectionEventTypeRepository.remove(cet)
        ()
      }
    )
  }

  val ErrMsgNameExists = "collection event type with name already exists"

  private def nameAvailable(name: String): DomainValidation[Boolean] = {
    nameAvailableMatcher(name, collectionEventTypeRepository, ErrMsgNameExists) { item =>
      item.name == name
    }
  }

  private def nameAvailable(name: String, excludeId: CollectionEventTypeId): DomainValidation[Boolean] = {
    nameAvailableMatcher(name, collectionEventTypeRepository, ErrMsgNameExists){ item =>
      (item.name == name) && (item.id != excludeId)
    }
  }

  /**
    * Checks that each specimen group belongs to the same study as the collection event type. If
    * one or more specimen groups are found that belong to a different study, they are returned in
    * the DomainError.
    */
  private def validateSpecimenGroupData(
    studyId: StudyId,
    specimenGroupData: List[CollectionEventTypeSpecimenGroupData])
      : DomainValidation[Boolean] = {
    val invalidSet = specimenGroupData.map(v => SpecimenGroupId(v.specimenGroupId)).map { id =>
      (id -> specimenGroupRepository.withId(studyId, id).isSuccess)
    }.filter(x => !x._2).map(_._1)

    if (invalidSet.isEmpty) true.success
    else DomainError("specimen group(s) do not belong to study: " + invalidSet.mkString(", ")).failureNel
  }

  /**
    * Checks that each annotation type belongs to the same study as the collection event type. If
    * one or more annotation types are found that belong to a different study, they are returned in
    * the DomainError.
    */
  private def validateAnnotationTypeData(
    studyId: StudyId,
    annotationTypeData: List[CollectionEventTypeAnnotationTypeData]): DomainValidation[Boolean] = {

    val invalidSet = annotationTypeData.map(v => AnnotationTypeId(v.annotationTypeId)).map { id =>
      (id -> collectionEventAnnotationTypeRepository.withId(studyId, id).isSuccess)
    }.filter(x => !x._2).map(_._1)

    if (invalidSet.isEmpty) true.success
    else DomainError("annotation type(s) do not belong to study: " + invalidSet.mkString(", ")).failureNel
  }

  private def convertSpecimenGroupDataToEvent
    (sgData: List[CollectionEventTypeSpecimenGroupData])
      : Seq[CollectionEventTypeAddedEvent.SpecimenGroupData] = {
    sgData.map { sg =>
      CollectionEventTypeAddedEvent.SpecimenGroupData(
        specimenGroupId = sg.specimenGroupId,
        maxCount        = Some(sg.maxCount),
        amount          = sg.amount.map(_.doubleValue)
      )
    }
  }

  private def convertSpecimenGroupDataFromEvent
    (sgData: Seq[CollectionEventTypeAddedEvent.SpecimenGroupData])
      : List[CollectionEventTypeSpecimenGroupData] = {
    sgData.map { sg =>
      CollectionEventTypeSpecimenGroupData(
        specimenGroupId = sg.specimenGroupId,
        maxCount        = sg.getMaxCount,
        amount          = sg.amount.map(BigDecimal(_))
      )
    } toList
  }

}
