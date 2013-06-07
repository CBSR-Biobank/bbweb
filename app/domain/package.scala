import scalaz.Validation

import domain.ConcurrencySafeEntity

package object domain {
  type DomainValidation[A] = Validation[DomainError, A]
  type DomainError = List[String]

  object DomainError {
    def apply(msg: String): DomainError = List(msg)
  }
}
