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

    case class IdNotFound(msg: String) extends ValidationMsgKey

    case class InvalidStatus(msg: String) extends ValidationMsgKey

  }

}
