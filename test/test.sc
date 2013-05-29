package test

import domain._
import scala.reflect._

object test {
   classTag[String]                               //> res0: scala.reflect.ClassTag[String] = java.lang.String
   val ng = new NameGenerator("123456789012345678901234567890")
                                                  //> ng  : test.NameGenerator = 1234567890123456789012...
   ng.next[String]                                //> res1: String = 1234567890123456789012..._1
   ng.next[String]                                //> res2: String = 1234567890123456789012..._2
   ng.next[domain.study.Study]                    //> res3: String = 1234567890123456789012..._1
   ng.next[domain.study.Study]                    //> res4: String = 1234567890123456789012..._2
   ng.next[domain.study.Study]                    //> res5: String = 1234567890123456789012..._3
   ng.next[domain.study.Study]                    //> res6: String = 1234567890123456789012..._4
   ng.next[String]                                //> res7: String = 1234567890123456789012..._3
}