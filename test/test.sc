package test

import domain._

object test {
   val ng = new NameGenerator("abcde12345abcde12345ab")
                                                  //> ng  : test.NameGenerator = abcde12345abcde12345ab...
   ng.next(classOf[String])                       //> res0: String = abcde12345abcde12345ab..._1
   ng.next(classOf[String])                       //> res1: String = abcde12345abcde12345ab..._2
   ng.next(classOf[domain.study.Study])           //> res2: String = abcde12345abcde12345ab..._1
   ng.next(classOf[domain.study.Study])           //> res3: String = abcde12345abcde12345ab..._2
}