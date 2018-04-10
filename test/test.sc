import domain._
import domain.studies._

import scalaz._
import scalaz.Scalaz._

object test {
  val x = "test1".successNel[String]              //> x  : scalaz.ValidationNel[String,String] = Success(test1)
  val y = "test2".failureNel[String]              //> y  : scalaz.ValidationNel[String,String] = Failure(NonEmptyList(test2))

  val z = (x |@| y){ _ + _ }                      //> z  : scalaz.Unapply[scalaz.Apply,scalaz.ValidationNel[String,String]]{type M
                                                  //| [X] = scalaz.ValidationNel[String,X]; type A = String}#M[String] = Failure(N
                                                  //| onEmptyList(test2))

  z match {
  case Success(x) =>
  case Failure(msg) =>
     msg.list.mkString(", ")
  }                                               //> res0: Any = test2
}
