import scalaz._
import scalaz.Scalaz._

import domain.AnatomicalSourceType
import AnatomicalSourceType._

object test {
  val v = AnatomicalSourceType.values             //> v  : domain.AnatomicalSourceType.ValueSet = AnatomicalSourceType.ValueSet(Bl
                                                  //| ood, Brain, Colon, Kidney, Ascending Colon, Descending Colon, Transverse Col
                                                  //| on, Duodenum, Ileum, Jejenum, Stomach Antrum, Stomach Body, Toe Nails, Urine
                                                  //| )
  val blood = AnatomicalSourceType.withName("Blood")
                                                  //> blood  : domain.AnatomicalSourceType.Value = Blood
}