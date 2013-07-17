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

  type UserReadRepository = ReadRepository[UserId, User]

  type UserReadWriteRepository = ReadWriteRepository[UserId, User]

}