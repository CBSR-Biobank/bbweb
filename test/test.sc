import domain._
import domain.study._
import scalaz._
import Scalaz._
object test {
  val test = Seq("aa", "bb", "cc")                //> test  : Seq[String] = List(aa, bb, cc)
  test.map(sg => "$(\"#" + sg +"\").popover();").mkString("\n")
                                                  //> res0: String = $("#aa").popover();
                                                  //| $("#bb").popover();
                                                  //| $("#cc").popover();
  (1 to 2)                                        //> res1: scala.collection.immutable.Range.Inclusive = Range(1, 2)
  (1 until 3)                                     //> res2: scala.collection.immutable.Range = Range(1, 2)
}