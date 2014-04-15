package org.biobank

import org.biobank.domain.study._

import scalaz._
import scalaz.Scalaz._

package object domain {

  type DomainValidation[A] = ValidationNel[DomainError, A]
  type DomainError = String

  object DomainError {
    def apply(msg: String): DomainError = msg
  }

  trait HasName {
    /** A short identifying name. */
    val name: String

  }

  trait HasUniqueName {
    /** A short identifying name that is unique. */
    val name: String
  }

  trait HasDescription {

    /** An description that can provide additional details on the name. */
    val description: String

  }

  /** A trait that can be used to define the properties for an entity.
    */
  trait HasDescriptionOption {

    /** An optional description that can provide additional details on the name. */
    val description: Option[String]

  }

  //trait HasAddedBy { val addedBy: UserId }
  //trait HasTimeAdded { val timeAdded: Long }
  //trait HasUpdatedBy { val updatedBy: Option[UserId] }
  //trait HasTimeUpdated { val timeUpdated: Option[Long] }

  type UserReadRepository = ReadRepository[UserId, User]

  type UserReadWriteRepository = ReadWriteRepository[UserId, User]

}
