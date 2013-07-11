import scalaz._
import scalaz.Scalaz._

import domain.AnatomicalSourceType
import AnatomicalSourceType._

object test {
  val x = Set("a", "b", "c")                      //> x  : scala.collection.immutable.Set[String] = Set(a, b, c)
  x.map{y => (y,y)} toMap                         //> res0: scala.collection.immutable.Map[String,String] = Map(a -> a, b -> b, c 
                                                  //| -> c)
}