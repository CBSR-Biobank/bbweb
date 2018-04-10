package org.biobank

import scalaz._

package service {

  class FilterString(val expression: String) extends AnyVal {
    override def toString: String = expression
  }

  class SortString(val expression: String) extends AnyVal {
    override def toString: String = expression
  }

  /** Factory object to create a domain error. */
  object ServiceError {
    def apply(msg: String): ServiceError = msg
  }

}

// move package object here due to: https://issues.scala-lang.org/browse/SI-9922
package object service {

  /** Used to validate commands received by the system at the service layer. */
  type ServiceValidation[A] = ValidationNel[ServiceError, A]

  /** Contains an error messsage when an invalid condition happens. */
  type ServiceError = String

}
