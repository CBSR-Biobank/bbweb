package org.biobank.service

import org.biobank.domain.{EntityState, HasState, HasStatePredicates, PredicateHelper}
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

trait EntityStateFilter[A <: HasState] extends PredicateHelper with HasStatePredicates[A] {
  import org.biobank.CommonValidations._
  import Comparator._

  protected def stateFilter(comparator:  Comparator,
                            stateNames:  List[String],
                            validStates: List[EntityState]):
      ServiceValidation[EntityStateFilter] = {
    stateNames.
      map { str =>
        validStates.find(_.id == str).toSuccessNel(
          InvalidState(s"entity state does not exist: $str").toString)
      }.
      toList.
      sequenceU.
      flatMap { states =>
        val stateSet = states.toSet
        comparator match {
          case Equal | In =>
            stateIsOneOf(stateSet).successNel[String]
          case NotEqualTo | NotIn =>
            complement(stateIsOneOf(stateSet)).successNel[String]
          case _ =>
            ServiceError(s"invalid filter on state: $comparator").failureNel[EntityStateFilter]
        }
      }
  }

}
