package org.biobank.service.study

import org.biobank.domain._
import org.biobank.domain.user.UserId
import org.biobank.domain.study.{
  Study,
  StudyId,
  CollectionEventType,
  CollectionEventTypeId,
  CollectionEventTypeRepositoryComponent,
  CollectionEventAnnotationTypeRepositoryComponent,
  SpecimenGroupId,
  SpecimenGroupRepositoryComponent
}
import org.slf4j.LoggerFactory
import org.biobank.service.{ Processor, WrappedCommand, WrappedEvent }
import org.biobank.infrastructure._
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.infrastructure.event.StudyEvents._

import akka.persistence.SnapshotOffer
import org.joda.time.DateTime
import scalaz._
import scalaz.Scalaz._

trait CollectionEventTypeProcessorComponent {
  self: CollectionEventTypeRepositoryComponent
      with CollectionEventAnnotationTypeRepositoryComponent
      with SpecimenGroupRepositoryComponent =>

  /**
    * The CollectionEventTypeProcessor is responsible for maintaining state changes for all
    * [[org.biobank.domain.study.CollectionEventType]] aggregates. This particular processor uses
    * Akka-Persistence's [[akka.persistence.PersistentActor]]. It receives Commands and if valid will persist
    * the generated events, afterwhich it will updated the current state of the
    * [[org.biobank.domain.study.CollectionEventType]] being processed.
    *
    * It is a child actor of [[org.biobank.service.study.StudiesProcessorComponent.StudiesProcessor]].
    */
  class CollectionEventTypeProcessor extends Processor {

    override def persistenceId = "collection-event-processor-id"

    case class SnapshotState(collectionEventTypes: Set[CollectionEventType])

    /**
      * These are the events that are recovered during journal recovery. They cannot fail and must be
      * processed to recreate the current state of the aggregate.
      */
    val receiveRecover: Receive = {
      case wevent: WrappedEvent[_] =>
        wevent.event match {
          case event: CollectionEventTypeAddedEvent =>   recoverEvent(event, wevent.userId, wevent.dateTime)
          case event: CollectionEventTypeUpdatedEvent => recoverEvent(event, wevent.userId, wevent.dateTime)
          case event: CollectionEventTypeRemovedEvent => recoverEvent(event, wevent.userId, wevent.dateTime)

          case event => throw new IllegalStateException(s"event not handled: $event")
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
          case cmd: AddCollectionEventTypeCmd =>    process(validateCmd(cmd)){ wevent => recoverEvent(wevent.event, wevent.userId, wevent.dateTime) }
          case cmd: UpdateCollectionEventTypeCmd => process(validateCmd(cmd)){ wevent => recoverEvent(wevent.event, wevent.userId, wevent.dateTime) }
          case cmd: RemoveCollectionEventTypeCmd => process(validateCmd(cmd)){ wevent => recoverEvent(wevent.event, wevent.userId, wevent.dateTime) }
        }

      case "snap" =>
        saveSnapshot(SnapshotState(collectionEventTypeRepository.getValues.toSet))
        stash()

      case cmd => log.error(s"message not handled: $cmd")
    }

    def update
      (cmd: CollectionEventTypeCommand)
      (fn: CollectionEventType => DomainValidation[CollectionEventType])
        : DomainValidation[CollectionEventType] = {
      for {
        cet <- collectionEventTypeRepository.withId(StudyId(cmd.studyId), CollectionEventTypeId(cmd.id))
        validVersion <-  cet.requireVersion(cmd.expectedVersion)
        updatedCet   <- fn(cet)
      } yield updatedCet
    }

    private def validateCmd(cmd: AddCollectionEventTypeCmd)
        : DomainValidation[CollectionEventTypeAddedEvent] = {
      val timeNow = DateTime.now
      val studyId = StudyId(cmd.studyId)
      val id = collectionEventTypeRepository.nextIdentity

      for {
        nameValid <- nameAvailable(cmd.name)
        newItem <- CollectionEventType.create(
          studyId, id, -1L, timeNow, cmd.name, cmd.description, cmd.recurring,
          cmd.specimenGroupData, cmd.annotationTypeData)
        validSgData <- validateSpecimenGroupData(studyId, cmd.specimenGroupData)
        validAtData <- validateAnnotationTypeData(studyId, cmd.annotationTypeData)
        event <- CollectionEventTypeAddedEvent(
          cmd.studyId, id.id, timeNow, newItem.name, newItem.description,
          newItem.recurring, newItem.specimenGroupData, newItem.annotationTypeData).success
      } yield event
    }

    private def validateCmd(
      cmd: UpdateCollectionEventTypeCmd): DomainValidation[CollectionEventTypeUpdatedEvent] = {
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

      v.fold(
        err => DomainError(s"error $err occurred on $cmd").failNel,
        cet => CollectionEventTypeUpdatedEvent(
          cmd.studyId, cet.id.id, cet.version, timeNow, cet.name, cet.description,
          cet.recurring, cet.specimenGroupData, cet.annotationTypeData).success
      )
    }

    private def validateCmd(
      cmd: RemoveCollectionEventTypeCmd): DomainValidation[CollectionEventTypeRemovedEvent] = {
      val v = update(cmd) { at => at.success }

      v.fold(
        err => DomainError(s"error $err occurred on $cmd").failNel,
        cet =>  CollectionEventTypeRemovedEvent(cet.studyId.id, cet.id.id).success
      )
    }

    private def recoverEvent(event: CollectionEventTypeAddedEvent, userId: UserId, dateTime: DateTime): Unit = {
      collectionEventTypeRepository.put(CollectionEventType(
        StudyId(event.studyId), CollectionEventTypeId(event.collectionEventTypeId), 0L, event.dateTime, None,
        event.name, event.description, event.recurring, event.specimenGroupData,
        event.annotationTypeData))
      ()
    }

    private def recoverEvent(event: CollectionEventTypeUpdatedEvent, userId: UserId, dateTime: DateTime): Unit = {
      collectionEventTypeRepository.getByKey(CollectionEventTypeId(event.collectionEventTypeId)).fold(
        err => throw new IllegalStateException(s"updating collection event type from event failed: $err"),
        cet => collectionEventTypeRepository.put(cet.copy(
          version            = event.version,
          timeModified     = Some(event.dateTime),
          name               = event.name,
          description        = event.description,
          recurring          = event.recurring,
          specimenGroupData  = event.specimenGroupData,
          annotationTypeData = event.annotationTypeData
        ))
      )
      ()
    }

    private def recoverEvent(event: CollectionEventTypeRemovedEvent, userId: UserId, dateTime: DateTime): Unit = {
      collectionEventTypeRepository.getByKey(CollectionEventTypeId(event.collectionEventTypeId)).fold(
        err => throw new IllegalStateException(s"updating collection event type from event failed: $err"),
        cet => collectionEventTypeRepository.remove(cet)
      )
      ()
    }

    val ErrMsgNameExists = "collection event type with name already exists"

    private def nameAvailable(name: String): DomainValidation[Boolean] = {
      nameAvailableMatcher(name, collectionEventTypeRepository, ErrMsgNameExists) { item =>
        item.name.equals(name)
      }
    }

    private def nameAvailable(name: String, excludeId: CollectionEventTypeId): DomainValidation[Boolean] = {
      nameAvailableMatcher(name, collectionEventTypeRepository, ErrMsgNameExists){ item =>
        item.name.equals(name) && (item.id != excludeId)
      }
    }

    /**
      * Checks that each specimen group belongs to the same study as the collection event type. If
      * one or more specimen groups are found that belong to a different study, they are returned in
      * the DomainError.
      */
    private def validateSpecimenGroupData(
      studyId: StudyId,
      specimenGroupData: List[CollectionEventTypeSpecimenGroupData]): DomainValidation[Boolean] = {

      val invalidSet = specimenGroupData.map(v => SpecimenGroupId(v.specimenGroupId)).map { id =>
        (id -> specimenGroupRepository.withId(studyId, id).isSuccess)
      }.filter(x => !x._2).map(_._1)

      if (invalidSet.isEmpty) true.success
      else DomainError("specimen group(s) do not belong to study: " + invalidSet.mkString(", ")).failNel
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
      else DomainError("annotation type(s) do not belong to study: " + invalidSet.mkString(", ")).failNel
    }

  }

}
