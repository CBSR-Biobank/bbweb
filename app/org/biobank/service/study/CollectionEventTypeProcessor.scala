package org.biobank.service.study

import org.biobank.domain._
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
import org.biobank.service._
import org.biobank.infrastructure._
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.infrastructure.event.StudyEvents._

import akka.persistence.SnapshotOffer
import scalaz._
import scalaz.Scalaz._

trait CollectionEventTypeProcessorComponent {
  self: CollectionEventTypeRepositoryComponent
      with CollectionEventAnnotationTypeRepositoryComponent
      with SpecimenGroupRepositoryComponent =>

  /**
    * This is the Collection Event Type processor. It is a child actor of
    * [[org.biobank.service.study.StudiesProcessorComponent.StudiesProcessor]].
    *
    * It handles commands that deal with a Collection Event Type.
    */
  class CollectionEventTypeProcessor extends Processor {

    override def persistenceId = "collection-event-processor-id"

    case class SnapshotState(collectionEventTypes: Set[CollectionEventType])

    val receiveRecover: Receive = {
      case event: CollectionEventTypeAddedEvent => recoverEvent(event)

      case event: CollectionEventTypeUpdatedEvent => recoverEvent(event)

      case event: CollectionEventTypeRemovedEvent => recoverEvent(event)

      case SnapshotOffer(_, snapshot: SnapshotState) =>
        snapshot.collectionEventTypes.foreach{ ceType =>
          collectionEventTypeRepository.put(ceType) }
    }


    val receiveCommand: Receive = {

      case cmd: AddCollectionEventTypeCmd => process(validateCmd(cmd)){ event => recoverEvent(event) }

      case cmd: UpdateCollectionEventTypeCmd => process(validateCmd(cmd)){ event => recoverEvent(event) }

      case cmd: RemoveCollectionEventTypeCmd => process(validateCmd(cmd)){ event => recoverEvent(event) }

      case _ =>
        throw new Error("invalid message received")
    }

    private def validateCmd(
      cmd: AddCollectionEventTypeCmd): DomainValidation[CollectionEventTypeAddedEvent] = {

      val studyId = StudyId(cmd.studyId)
      val id = collectionEventTypeRepository.nextIdentity

      for {
        nameValid <- nameAvailable(cmd.name)
        newItem <- CollectionEventType.create(
          studyId, id, -1L, org.joda.time.DateTime.now, cmd.name, cmd.description, cmd.recurring,
          cmd.specimenGroupData, cmd.annotationTypeData)
        validSgData <- validateSpecimenGroupData(studyId, cmd.specimenGroupData)
        validAtData <- validateAnnotationTypeData(studyId, cmd.annotationTypeData)
        event <- CollectionEventTypeAddedEvent(
          cmd.studyId, id.id, newItem.addedDate, newItem.name, newItem.description,
          newItem.recurring, newItem.specimenGroupData, newItem.annotationTypeData).success
      } yield event
    }

    private def validateCmd(
      cmd: UpdateCollectionEventTypeCmd): DomainValidation[CollectionEventTypeUpdatedEvent] = {
      val studyId = StudyId(cmd.studyId)
      val id = CollectionEventTypeId(cmd.id)

      for {
        oldItem <- collectionEventTypeRepository.withId(studyId,id)
        nameValid <- nameAvailable(cmd.name, id)
        newItem <- oldItem.update(
         Some(cmd.expectedVersion), org.joda.time.DateTime.now, cmd.name,
          cmd.description, cmd.recurring, cmd.specimenGroupData, cmd.annotationTypeData)
        validSgData <- validateSpecimenGroupData(studyId, cmd.specimenGroupData)
        validAtData <- validateAnnotationTypeData(studyId, cmd.annotationTypeData)
        event <- CollectionEventTypeUpdatedEvent(
          cmd.studyId, newItem.id.id, newItem.version, newItem.lastUpdateDate.get, newItem.name,
          newItem.description, newItem.recurring, newItem.specimenGroupData,
          newItem.annotationTypeData).success
      } yield event
    }

    private def validateCmd(
      cmd: RemoveCollectionEventTypeCmd): DomainValidation[CollectionEventTypeRemovedEvent] = {
      val studyId = StudyId(cmd.studyId)
      val id = CollectionEventTypeId(cmd.id)

      for {
        item <- collectionEventTypeRepository.withId(studyId, id)
        validVersion <- validateVersion(item,Some(cmd.expectedVersion))
        event <- CollectionEventTypeRemovedEvent(cmd.studyId, cmd.id).success
      } yield event
    }

    private def recoverEvent(event: CollectionEventTypeAddedEvent): Unit = {
      val studyId = StudyId(event.studyId)
      val validation = for {
        newItem <- CollectionEventType.create(
          studyId, CollectionEventTypeId(event.collectionEventTypeId), -1L, event.dateTime,
          event.name, event.description, event.recurring, event.specimenGroupData,
          event.annotationTypeData)
        savedItem <- collectionEventTypeRepository.put(newItem).success
      } yield newItem

      if (validation.isFailure) {
        // this should never happen because the only way to get here is when the
        // command passed validation
        throw new IllegalStateException("recovering collection event type from event failed")
      }
    }

    private def recoverEvent(event: CollectionEventTypeUpdatedEvent): Unit = {
      val validation = for {
        item <- collectionEventTypeRepository.getByKey(CollectionEventTypeId(event.collectionEventTypeId))
        updatedItem <- item.update(
          item.versionOption, event.dateTime, event.name,
          event.description, event.recurring, event.specimenGroupData, event.annotationTypeData)
        savedItem <- collectionEventTypeRepository.put(updatedItem).success
      } yield updatedItem

      if (validation.isFailure) {
        // this should never happen because the only way to get here is when the
        // command passed validation
        val err = validation.swap.getOrElse(List.empty)
        throw new IllegalStateException(
          s"recovering collection event type update from event failed: $err")
      }
    }

    private def recoverEvent(event: CollectionEventTypeRemovedEvent): Unit = {
      val validation = for {
        item <- collectionEventTypeRepository.getByKey(CollectionEventTypeId(event.collectionEventTypeId))
        removedItem <- collectionEventTypeRepository.remove(item).success
      } yield removedItem

      if (validation.isFailure) {
        // this should never happen because the only way to get here is when the
        // command passed validation
        val err = validation.swap.getOrElse(List.empty)
        throw new IllegalStateException(
          s"recovering collection event type remove from event failed: $err")
      }
    }

    val errMsgNameExists = "collection event type with name already exists"

    private def nameAvailable(name: String): DomainValidation[Boolean] = {
      nameAvailableMatcher(name, collectionEventTypeRepository, errMsgNameExists) { item =>
        item.name.equals(name)
      }
    }

    private def nameAvailable(name: String, excludeId: CollectionEventTypeId): DomainValidation[Boolean] = {
      nameAvailableMatcher(name, collectionEventTypeRepository, errMsgNameExists){ item =>
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
