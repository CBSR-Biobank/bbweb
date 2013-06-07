import scalaz._
import scalaz.Scalaz._

import domain._

object test {
  def even(x: Int) =
    if ((x % 2) == 0) x.success
    else ("not even: %d" format x).fail           //> even: (x: Int)scalaz.Validation[String,Int]
  
  val x1 = 1.success                              //> x1  : scalaz.Validation[Nothing,Int] = Success(1)
  val x2 = "a".fail                               //> x2  : scalaz.Validation[String,Nothing] = Failure(a)
  
  def y1(a: Int): DomainValidation[Int] = (a + 2).success
                                                  //> y1: (a: Int)domain.DomainValidation[Int]
  def y2(a: Int): DomainValidation[String] = ("test" + a).success
                                                  //> y2: (a: Int)domain.DomainValidation[String]
  
  x1 flatMap y1                                   //> res0: scalaz.Validation[domain.DomainError,Int] = Success(3)
  x2 flatMap y1                                   //> res1: scalaz.Validation[Object,Int] = Failure(a)
  even(1) <|*|> even(2)                           //> res2: scalaz.Validation[String,(Int, Int)] = Failure(not even: 1)
  even(4) <|*|> even(2)                           //> res3: scalaz.Validation[String,(Int, Int)] = Success((4,2))
  even(4) <|*|> "a".success                       //> res4: scalaz.Validation[String,(Int, String)] = Success((4,a))
  y1(1) <|*|> y2(2)                               //> res5: scalaz.Validation[domain.DomainError,(Int, String)] = Success((3,test2
                                                  //| ))
}