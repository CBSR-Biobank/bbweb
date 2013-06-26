import scalaz._
import scalaz.Scalaz._

import domain.AnatomicalSourceType
import AnatomicalSourceType._

object test {
AnatomicalSourceType.values.map(x => (x.toString -> x.toString))
                                                  //> res0: scala.collection.immutable.SortedSet[(String, String)] = TreeSet((Asce
                                                  //| nding Colon,Ascending Colon), (Blood,Blood), (Brain,Brain), (Colon,Colon), (
                                                  //| Descending Colon,Descending Colon), (Duodenum,Duodenum), (Ileum,Ileum), (Kid
                                                  //| ney,Kidney), (Stomach Antrum,Stomach Antrum), (Stomach Body,Stomach Body), (
                                                  //| Toe Nails,Toe Nails), (Transverse Colon,Transverse Colon), (Urine,Urine))
}