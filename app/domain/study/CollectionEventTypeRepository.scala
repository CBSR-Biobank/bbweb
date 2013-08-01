package domain.study

import domain._

import org.slf4j.LoggerFactory

import scalaz._
import Scalaz._

trait CollectionEventTypeRepositoryComponent {

  val collectionEventTypeRepository: CollectionEventTypeRepository

  trait CollectionEventTypeRepository {

    def collectionEventTypeWithId(
      studyId: StudyId,
      ceventTypeId: CollectionEventTypeId): DomainValidation[CollectionEventType]

    def allCollectionEventTypesForStudy(studyId: StudyId): Set[CollectionEventType]

    def specimenGroupInUse(specimenGroup: SpecimenGroup): Boolean

    def annotationTypeInUse(annotationType: CollectionEventAnnotationType): Boolean

    def add(ceventType: CollectionEventType): DomainValidation[CollectionEventType]

    def update(ceventType: CollectionEventType): DomainValidation[CollectionEventType]

    def remove(ceventType: CollectionEventType): DomainValidation[CollectionEventType]

  }
}

trait CollectionEventTypeRepositoryComponentImpl extends CollectionEventTypeRepositoryComponent {

  override val collectionEventTypeRepository: CollectionEventTypeRepository = new CollectionEventTypeRepositoryImpl

  class CollectionEventTypeRepositoryImpl
    extends ReadWriteRepository[CollectionEventTypeId, CollectionEventType](v => v.id)
    with CollectionEventTypeRepository {

    val log = LoggerFactory.getLogger(this.getClass)

    def collectionEventTypeWithId(
      studyId: StudyId,
      ceventTypeId: CollectionEventTypeId): DomainValidation[CollectionEventType] = {
      getByKey(ceventTypeId) match {
        case Failure(x) =>
          DomainError(
            "collection event type does not exist: { studyId: %s, ceventTypeId: %s }".format(
              studyId, ceventTypeId)).fail
        case Success(cet) =>
          if (cet.studyId.equals(studyId))
            cet.success
          else DomainError(
            "study does not have collection event type: { studyId: %s, ceventTypeId: %s }".format(
              studyId, ceventTypeId)).fail
      }
    }

    def allCollectionEventTypesForStudy(studyId: StudyId): Set[CollectionEventType] = {
      getValues.filter(x => x.studyId.equals(studyId)).toSet
    }

    private def nameAvailable(ceventType: CollectionEventType): DomainValidation[Boolean] = {
      val exists = getValues.exists { item =>
        item.studyId.equals(ceventType.studyId) &&
          item.name.equals(ceventType.name) &&
          !item.id.equals(ceventType.id)
      }

      if (exists)
        DomainError("collection event type with name already exists: %s" format ceventType.name).fail
      else
        true.success
    }

    def specimenGroupInUse(specimenGroup: SpecimenGroup): Boolean = {
      val studyCeventTypes = getValues.filter(cet => cet.studyId.equals(specimenGroup.studyId))
      studyCeventTypes.exists(cet =>
        cet.specimenGroupData.exists(sgd =>
          sgd.specimenGroupId.equals(specimenGroup.id.id)))
    }

    def annotationTypeInUse(annotationType: CollectionEventAnnotationType): Boolean = {
      val studyCeventTypes = getValues.filter(cet => cet.studyId.equals(annotationType.studyId))
      studyCeventTypes.exists(cet =>
        cet.annotationTypeData.exists(atd =>
          atd.annotationTypeId.equals(annotationType.id.id)))
    }

    def add(ceventType: CollectionEventType): DomainValidation[CollectionEventType] = {
      collectionEventTypeWithId(ceventType.studyId, ceventType.id) match {
        case Success(prevItem) =>
          DomainError("collection event type with ID already exists: %s" format ceventType.id).fail
        case Failure(x) =>
          for {
            nameValid <- nameAvailable(ceventType)
            item <- updateMap(ceventType).success
          } yield item
      }
    }

    def update(ceventType: CollectionEventType): DomainValidation[CollectionEventType] = {
      for {
        prevItem <- collectionEventTypeWithId(ceventType.studyId, ceventType.id)
        validVersion <- prevItem.requireVersion(Some(ceventType.version))
        nameValid <- nameAvailable(ceventType)
        updatedItem <- CollectionEventType(
          ceventType.id, ceventType.version + 1, ceventType.studyId,
          ceventType.name, ceventType.description, ceventType.recurring, ceventType.specimenGroupData,
          ceventType.annotationTypeData).success
        updatedItem <- updateMap(updatedItem).success
      } yield updatedItem
    }

    def remove(ceventType: CollectionEventType): DomainValidation[CollectionEventType] = {
      for {
        item <- collectionEventTypeWithId(ceventType.studyId, ceventType.id)
        validVersion <- item.requireVersion(Some(ceventType.version))
        removedItem <- removeFromMap(item).success
      } yield removedItem
    }
  }
}
