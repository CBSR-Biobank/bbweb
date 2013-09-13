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
}