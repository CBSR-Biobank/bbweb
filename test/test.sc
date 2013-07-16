import scalaz._
import Scalaz._

object test {
  type DomainValidation[A] = Validation[DomainError, A]
  type DomainError = List[String]

  object DomainError {
    def apply(msg: String): DomainError = List(msg)
  }
  
   val x = DomainError("error").fail              //> x  : scalaz.Validation[test.DomainError,Nothing] = Failure(List(error))
   (x | DomainError("fail"))                      //> res0: test.DomainError = List(fail)
}