package org.biobank.service

import org.biobank.domain.{HasName, HasNamePredicates, PredicateHelper}
import scalaz.Scalaz._

trait EntityNameFilter[A <: HasName] extends PredicateHelper with HasNamePredicates[A] {
  import Comparator._

  protected def nameFilter(comparator: Comparator, names: List[String]):
      ServiceValidation[EntityNameFilter] = {
    val nameSet = names.toSet
    comparator match {
      case Equal =>
        if ((names.size == 1) && names(0).contains("*")) {
          nameContains(names(0)).successNel[String]
        } else {
          nameIsOneOf(nameSet).successNel[String]
        }
      case In =>
        nameIsOneOf(nameSet).successNel[String]
      case NotEqualTo | NotIn =>
        complement(nameIsOneOf(nameSet)).successNel[String]
      case _ =>
        ServiceError(s"invalid filter on name: $comparator").failureNel[EntityNameFilter]
    }
  }

}
