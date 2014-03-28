package service.study

import domain._
import domain.study._
import domain.study.Study._
import org.slf4j.LoggerFactory
import service._
import service.commands.StudyCommands._
import service.events.StudyEvents._

import scalaz._
import scalaz.Scalaz._

// FIXME: rename CollectionEventTypeService to CollectionEventTypeSubProcessor

/**
 * This is the Collection Event Type Domain Service.
 *
 * It handles commands that deal with a Collection Event Type.
 *
 * @param studyRepository The read-only repository for study entities.
 * @param collectionEventTypeRepository The repository for specimen group entities.
 * @param specimenGroupRepository The read-only repository for specimen group entities.
 * @param sg2cetRepo The value object repository that associates a specimen group to a
 *         collection event type.
 * @param at2cetRepo The value object repository that associates a collection event annotation
 *         type to a collection event type.
 */
trait CollectionEventTypeServiceComponent {
  self: RepositoryComponent =>

  val collectionEventTypeService = new CollectionEventTypeService

  class CollectionEventTypeService() extends CommandHandler {

    val log = LoggerFactory.getLogger(this.getClass)

    /**
     * This partial function handles each command. The command is contained within the
     * StudyAggregateMsg.
     *
     *  If the command is invalid, then this method throws an Error exception.
     */
    def process = {

      case msg: StudyAggregateMsg =>
        msg.cmd match {
          // collection event types
          case cmd: AddCollectionEventTypeCmd =>
            addCollectionEventType(cmd, msg.study, msg.id)
          case cmd: UpdateCollectionEventTypeCmd =>
            updateCollectionEventType(cmd, msg.study)
          case cmd: RemoveCollectionEventTypeCmd =>
            removeCollectionEventType(cmd, msg.study)

          case _ =>
            throw new Error("invalid command received")
        }

      case _ =>
        throw new Error("invalid message received")
    }

    /**
     * Checks that each specimen group belongs to the same study as the collection event type. If
     * one or more specimen groups are found that belong to a different study, they are returned in
     * the DomainError.
     */
    private def validateSpecimenGroupData(
      study: DisabledStudy,
      specimenGroupData: Set[CollectionEventTypeSpecimenGroup]): DomainValidation[Boolean] = {

      val invalidSet = specimenGroupData.map(v => SpecimenGroupId(v.specimenGroupId)).map { id =>
        (id -> specimenGroupRepository.specimenGroupWithId(study.id, id).isSuccess)
      }.filter(x => !x._2).map(_._1)

      if (invalidSet.isEmpty) true.success
      else DomainError("specimen group(s) do not belong to study: " + invalidSet.mkString(", ")).fail
    }

    /**
     * Checks that each annotation type belongs to the same study as the collection event type. If
     * one or more annotation types are found that belong to a different study, theyare returned in
     * the DomainError.
     */
    private def validateAnnotationTypeData(
      study: DisabledStudy,
      annotationTypeData: Set[CollectionEventTypeAnnotationType]): DomainValidation[Boolean] = {

      val invalidSet = annotationTypeData.map(v => AnnotationTypeId(v.annotationTypeId)).map { id =>
        (id -> collectionEventAnnotationTypeRepository.annotationTypeWithId(study.id, id).isSuccess)
      }.filter(x => !x._2).map(_._1)

      if (invalidSet.isEmpty) true.success
      else DomainError("annotation type(s) do not belong to study: " + invalidSet.mkString(", ")).fail
    }

    private def addCollectionEventType(
      cmd: AddCollectionEventTypeCmd,
      study: DisabledStudy,
      id: Option[String]): DomainValidation[CollectionEventTypeAddedEvent] = {

      for {
        cetId <- id.toSuccess(DomainError("collection event type ID is missing"))
        newItem <- CollectionEventType(
          CollectionEventTypeId(cetId), 0L, study.id, cmd.name, cmd.description, cmd.recurring,
          cmd.specimenGroupData, cmd.annotationTypeData).success
        validSgData <- validateSpecimenGroupData(study, newItem.specimenGroupData)
        validAtData <- validateAnnotationTypeData(study, newItem.annotationTypeData)
        addItem <- collectionEventTypeRepository.add(newItem)
        newEvent <- CollectionEventTypeAddedEvent(
          study.id.id, newItem.id.id, newItem.version, newItem.name, newItem.description,
          newItem.recurring, newItem.specimenGroupData, newItem.annotationTypeData).success
      } yield newEvent
    }

    private def updateCollectionEventType(
      cmd: UpdateCollectionEventTypeCmd,
      study: DisabledStudy): DomainValidation[CollectionEventTypeUpdatedEvent] = {

      for {
        oldItem <- collectionEventTypeRepository.collectionEventTypeWithId(
          study.id, CollectionEventTypeId(cmd.id))
        newItem <- collectionEventTypeRepository.update(CollectionEventType(
          CollectionEventTypeId(cmd.id), cmd.expectedVersion.getOrElse(-1), study.id, cmd.name,
          cmd.description, cmd.recurring, cmd.specimenGroupData, cmd.annotationTypeData))
        newEvent <- CollectionEventTypeUpdatedEvent(
          study.id.id, newItem.id.id, newItem.version, newItem.name, newItem.description,
          newItem.recurring, newItem.specimenGroupData, newItem.annotationTypeData).success
      } yield newEvent
    }

    private def removeCollectionEventType(
      cmd: RemoveCollectionEventTypeCmd,
      study: DisabledStudy): DomainValidation[CollectionEventTypeRemovedEvent] = {
      for {
        oldItem <- collectionEventTypeRepository.collectionEventTypeWithId(
          study.id, CollectionEventTypeId(cmd.id))
        itemToRemove <- CollectionEventType(
          CollectionEventTypeId(cmd.id), cmd.expectedVersion.getOrElse(-1), study.id,
          oldItem.name, oldItem.description, oldItem.recurring, oldItem.specimenGroupData,
          oldItem.annotationTypeData).success
        removedItem <- collectionEventTypeRepository.remove(itemToRemove)
        newEvent <- CollectionEventTypeRemovedEvent(
          removedItem.studyId.id, removedItem.id.id).success
      } yield newEvent
    }
  }
}
