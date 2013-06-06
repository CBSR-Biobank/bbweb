
import domain._
import domain.SpecimenType._

import scalaz._
import Scalaz._

object test {
  SpecimenType.values                             //> res0: domain.SpecimenType.ValueSet = SpecimenType.ValueSet(Buffy coat, CDPA 
                                                  //| Plasma, Centrifuged Urine, Cord Blood Mononuclear Cells, DNA (Blood), DNA (W
                                                  //| hite blood cells), Descending Colon, Duodenum, Filtered Urine, Finger Nails,
                                                  //|  Hair, Hemodialysate, Heparin Blood, Ileum, Jejunum, Lithium Heparin Plasma,
                                                  //|  Meconium - BABY, Paxgene, Peritoneal Dialysate, Plasma (Na Heparin) - DAD, 
                                                  //| Plasma, Platelet free plasma, RNA, RNA CBMC, RNAlater Biopsies, Serum, Sodiu
                                                  //| mAzideUrine, Source Water, Tap Water, Transverse Colon)
  val one: PartialFunction[Int, String] = { case 1 => "one" }
                                                  //> one  : PartialFunction[Int,String] = <function1>
  one(2)                                          //> scala.MatchError: 2 (of class java.lang.Integer)
                                                  //| 	at scala.PartialFunction$$anon$1.apply(PartialFunction.scala:248)
                                                  //| 	at scala.PartialFunction$$anon$1.apply(PartialFunction.scala:246)
                                                  //| 	at test$$anonfun$main$1$$anonfun$1.applyOrElse(test.scala:10)
                                                  //| 	at test$$anonfun$main$1$$anonfun$1.applyOrElse(test.scala:10)
                                                  //| 	at scala.runtime.AbstractPartialFunction$mcLI$sp.apply$mcLI$sp(AbstractP
                                                  //| artialFunction.scala:33)
                                                  //| 	at scala.runtime.AbstractPartialFunction$mcLI$sp.apply(AbstractPartialFu
                                                  //| nction.scala:33)
                                                  //| 	at scala.runtime.AbstractPartialFunction$mcLI$sp.apply(AbstractPartialFu
                                                  //| nction.scala:25)
                                                  //| 	at test$$anonfun$main$1.apply$mcV$sp(test.scala:11)
                                                  //| 	at org.scalaide.worksheet.runtime.library.WorksheetSupport$$anonfun$$exe
                                                  //| cute$1.apply$mcV$sp(WorksheetSupport.scala:76)
                                                  //| 	at org.scalaide.worksheet.runtime.library.WorksheetSupport$.redirected(W
                                                  //| orksheetSupport.scala:65)
                                                  //| 	at org.scalaide.worksheet.runtime.library.WorksheetSupport$.$execut
                                                  //| Output exceeds cutoff limit.
}