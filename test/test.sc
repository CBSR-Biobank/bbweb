import scalaz._
import scalaz.Scalaz._

import domain.AnatomicalSourceType
import AnatomicalSourceType._

object test {
  val x = Set("a", "b", "c")                      //> x  : scala.collection.immutable.Set[String] = Set(a, b, c)
  val y = Some(x.map(  v => (v,v) ).toMap)        //> y  : Some[scala.collection.immutable.Map[String,String]] = Some(Map(a -> a, 
                                                  //| b -> b, c -> c))
  y.map(v => v.values.mkString("\n")).getOrElse(None)
                                                  //> res0: java.io.Serializable = a
                                                  //| b
                                                  //| c
}