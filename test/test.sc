import domain._
import domain.study._
import scalaz._
import Scalaz._
object test {
  val x = Seq((1,2,3),(4,5,6),(7,8,9))
  x.map(y => "%d-%d".format(y._1, y._2)).mkString("\n")



















































}