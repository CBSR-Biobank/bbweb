package test

import domain._
import scala.reflect._

object test {
  /*
   classTag[String]
   val ng = new NameGenerator("123456789012345678901234567890")
   ng.next[String]
   ng.next[String]
   ng.next[domain.study.Study]
   ng.next[domain.study.Study]
   ng.next[domain.study.Study]
   ng.next[domain.study.Study]
   ng.next[String]
   */
   
   val m = Map[Int,String](1->"a",2->"b")         //> m  : scala.collection.immutable.Map[Int,String] = Map(1 -> a, 2 -> b)
   val m1 = m + (2 -> "c")                        //> m1  : scala.collection.immutable.Map[Int,String] = Map(1 -> a, 2 -> c)
}