package org

import scalaz._
import scalaz.Scalaz._

package biobank {

  /**
   * Trait for validation errors
   */
  trait ValidationKey {
    def failure[T]    = this.toString.failure[T]
    def failureNel[T] = this.toString.failureNel[T]
    def nel           = NonEmptyList(this.toString)
  }

  trait ValidationMsgKey extends ValidationKey {
    val msg : String
  }

  object CommonValidations {

    final case class InvalidVersion(msg: String) extends ValidationMsgKey

    final case class IdNotFound(msg: String) extends ValidationMsgKey {
      override val toString = s"IdNotFound: $msg"
    }

    final case class InvalidStatus(msg: String) extends ValidationMsgKey {
      override val toString = s"InvalidStatus($msg)"
    }

    case object InvalidToken extends ValidationKey {
      override val toString = "InvalidToken"
    }

    case object InvalidPassword extends ValidationKey {
      override val toString = "InvalidPassword"
    }

    final case class EmailNotFound(msg: String) extends ValidationMsgKey {
      override val toString = s"EmailNotFound: $msg"
    }

    final case class EmailNotAvailable(msg: String) extends ValidationMsgKey {
      override val toString = s"EmailNotAvailable: $msg"
    }

    final case class EntityCriteriaNotFound(msg: String) extends ValidationMsgKey {
      override val toString = s"EntityCriteriaNotFound: $msg"
    }

    final case class EntityCriteriaError(msg: String) extends ValidationMsgKey {
      override val toString = s"EntityCriteriaError: $msg"
    }

    final case class EntityRequired(msg: String) extends ValidationMsgKey {
      override val toString = s"EntityRequired: $msg"
    }

    case object LocationIdInvalid extends ValidationKey {
      override val toString = "LocationIdInvalid"
    }

    case object ContainerIdInvalid extends ValidationKey {
      override val toString = "ContainerIdInvalid"
    }

    case object SpecimenIdRequired extends ValidationKey {
      override val toString = "SpecimenIdRequired"
    }

    final case class EntityInUse(msg: String) extends ValidationMsgKey {
      override val toString = s"EntityInUse: $msg"
    }

  }

}
