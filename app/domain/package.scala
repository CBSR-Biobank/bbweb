import domain.study._

import scalaz._
import Scalaz._

package object domain {

  type DomainValidation[A] = Validation[DomainError, A]
  type DomainError = List[String]

  object DomainError {
    def apply(msg: String): DomainError = List(msg)
  }

  trait HasName { val name: String }
  trait HasDescription { val description: String }
  trait HasDescriptionOption { val description: Option[String] }

  //trait HasAddedBy { val addedBy: UserId }
  //trait HasTimeAdded { val timeAdded: Long }
  //trait HasUpdatedBy { val updatedBy: Option[UserId] }
  //trait HasTimeUpdated { val timeUpdated: Option[Long] } 

  type SpecimenGroupCollectionEventTypeReadRepository = ReadWriteRepository[String, SpecimenGroupCollectionEventType]

  type UserReadRepository = ReadRepository[UserId, User]

  type UserReadWriteRepository = ReadWriteRepository[UserId, User]

  object StudyRepository extends ReadRepository[StudyId, Study](v => v.id) {}

  object SpecimenGroupRepository
    extends ReadRepository[SpecimenGroupId, SpecimenGroup](v => v.id) {}

  object CollectionEventAnnotationTypeRepository
    extends ReadWriteRepository[AnnotationTypeId, CollectionEventAnnotationType](v => v.id) {}

  object CollectionEventTypeRepository
    extends ReadWriteRepository[CollectionEventTypeId, CollectionEventType](v => v.id) {}

  object SpecimenGroupCollectionEventTypeRepository
    extends ReadWriteRepository[String, SpecimenGroupCollectionEventType](v => v.id) {}

  object CollectionEventTypeAnnotationTypeRepository
    extends ReadWriteRepository[String, CollectionEventTypeAnnotationType](v => v.id) {}

}