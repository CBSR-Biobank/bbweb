import domain._
import domain.study._

import scalaz._
import Scalaz._

object test {
  val sg2study = Map(("a" -> "A"),("b" -> "B"))   //> sg2study  : scala.collection.immutable.Map[String,String] = Map(a -> A, b ->
                                                  //|  B)
  
  val specimenGroupData = Set(
    SpecimenGroupCollectionEventType("a", 5, 10.1), SpecimenGroupCollectionEventType("b", 10, 1.1)
    )                                             //> specimenGroupData  : scala.collection.immutable.Set[domain.study.SpecimenGro
                                                  //| upCollectionEventType] = Set(SpecimenGroupCollectionEventType(a,5,10.1), Spe
                                                  //| cimenGroupCollectionEventType(b,10,1.1))
    
  def specimenGroupWithId(studyId: String, sgId: String): DomainValidation[Boolean] = {
    sg2study.get(sgId) match {
      case Some(x) => x.equals(studyId) match {
        case true => true.success
        case false => DomainError("invalid study").fail
      }
      case None => DomainError("invalid sg").fail
    }
  }                                               //> specimenGroupWithId: (studyId: String, sgId: String)domain.DomainValidation[
                                                  //| Boolean]
  val y = specimenGroupData.map(v => v.specimenGroupId).map { id =>
      (id -> specimenGroupWithId("A", id).isSuccess)
    }.filter(x => !x._2)                          //> y  : scala.collection.immutable.Set[(String, Boolean)] = Set((b,false))
  if (y.isEmpty) true.success
  else DomainError("specimen group does not belong to study: " + y.head._1)
                                                  //> res0: Object = List(specimen group does not belong to study: b)
}