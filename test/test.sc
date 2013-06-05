
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
}