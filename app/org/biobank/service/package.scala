package org.biobank

import scalaz._

package object service {

  /** Used to validate commands received by the system at the service layer. */
  type ServiceValidation[A] = ValidationNel[ServiceError, A]

  /** Contains an error messsage when an invalid condition happens. */
  type ServiceError = String

}

package service {

  /** Factory object to create a domain error. */
  object ServiceError {
    def apply(msg: String): ServiceError = msg
  }

}
