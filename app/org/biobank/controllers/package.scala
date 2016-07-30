package org.biobank

import scalaz._

package object controllers {

  /** Used to validate commands received by the system at the controllers layer. */
  type ControllerValidation[A] = ValidationNel[ControllerError, A]

  /** Contains an error messsage when an invalid condition happens. */
  type ControllerError = String

}

package controllers {

  /** Factory object to create a domain error. */
  object ControllerError {
    def apply(msg: String): ControllerError = msg
  }

}
