import domain.study._

import scalaz._
import scalaz.Scalaz._

package object domain {

  type DomainValidation[A] = ValidationNel[DomainError, A]
  type DomainError = String

  object DomainError {
    def apply(msg: String): DomainError = msg
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