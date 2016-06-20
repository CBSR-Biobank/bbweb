package org

import scalaz._
import scalaz.Scalaz._

package biobank {

  /**
   * Trait for validation errors
   */
  trait ValidationKey {
    def failure       = this.toString.failure
    def failureNel[T] = this.toString.failureNel[T]
    def nel           = NonEmptyList(this.toString)
  }

  trait ValidationMsgKey extends ValidationKey {
    val msg : String
  }

  object CommonValidations {

    case class InvalidVersion(msg: String) extends ValidationMsgKey

    case class IdNotFound(msg: String) extends ValidationMsgKey

    case class InvalidStatus(msg: String) extends ValidationMsgKey

    case object InvalidToken extends ValidationKey

    case object InvalidPassword extends ValidationKey

    case class EmailNotFound(msg: String) extends ValidationMsgKey

    case class EmailNotAvailable(msg: String) extends ValidationMsgKey

    case class EntityCriteriaNotFound(msg: String) extends ValidationMsgKey

    case class EntityCriteriaError(msg: String) extends ValidationMsgKey

    case class EntityRequried(msg: String) extends ValidationMsgKey

    case object LocationIdInvalid extends ValidationKey

    case object ContainerIdInvalid extends ValidationKey

    case object SpecimenIdRequired extends ValidationKey

    case class EntityInUse(msg: String) extends ValidationMsgKey

  }

}
