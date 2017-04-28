package org

import scalaz._
import scalaz.Scalaz._

package biobank {

  /**
   * Trait for validation errors
   */
  trait ValidationKey {
    def failure[T]: Validation[String, T]       = this.toString.failure[T]
    def failureNel[T]: ValidationNel[String, T] = this.toString.failureNel[T]
    def nel: NonEmptyList[String]               = NonEmptyList(this.toString)
  }

  trait ValidationMsgKey extends ValidationKey {
    val msg : String
  }

  object CommonValidations {

    final case class InvalidVersion(msg: String) extends ValidationMsgKey

    case object Unauthorized extends ValidationKey {
      override val toString: String = "Unauthorized"
    }

    final case class IdNotFound(msg: String) extends ValidationMsgKey {
      override val toString: String = s"IdNotFound: $msg"
    }

    final case class InvalidStatus(msg: String) extends ValidationMsgKey {
      override val toString: String = s"InvalidStatus: $msg"
    }

    final case class InvalidState(msg: String) extends ValidationMsgKey {
      override val toString: String = s"InvalidState: $msg"
    }

    case object InvalidToken extends ValidationKey {
      override val toString: String = "InvalidToken"
    }

    case object InvalidPassword extends ValidationKey {
      override val toString: String = "InvalidPassword"
    }

    final case class EmailNotFound(msg: String) extends ValidationMsgKey {
      override val toString: String = s"EmailNotFound: $msg"
    }

    final case class EmailNotAvailable(msg: String) extends ValidationMsgKey {
      override val toString: String = s"EmailNotAvailable: $msg"
    }

    final case class EntityCriteriaNotFound(msg: String) extends ValidationMsgKey {
      override val toString: String = s"EntityCriteriaNotFound: $msg"
    }

    final case class EntityCriteriaError(msg: String) extends ValidationMsgKey {
      override val toString: String = s"EntityCriteriaError: $msg"
    }

    final case class EntityRequired(msg: String) extends ValidationMsgKey {
      override val toString: String = s"EntityRequired: $msg"
    }

    case object LocationIdInvalid extends ValidationKey {
      override val toString: String = "LocationIdInvalid"
    }

    case object ContainerIdInvalid extends ValidationKey {
      override val toString: String = "ContainerIdInvalid"
    }

    case object SpecimenIdRequired extends ValidationKey {
      override val toString: String = "SpecimenIdRequired"
    }

    final case class EntityInUse(msg: String) extends ValidationMsgKey {
      override val toString: String = s"EntityInUse: $msg"
    }

  }

}
