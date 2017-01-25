package org.biobank

import scalaz._

package domain {

  /** Factory object to create a domain error. */
  object DomainError {
    def apply(msg: String): DomainError = msg
  }

  class EntityState(val id: String) extends AnyVal {
    override def toString = id
  }

  trait HasState {
    /** the entity's current state. */
    val state: EntityState
  }

  trait HasName {
    /** A short identifying name. */
    val name: String

  }

  trait HasUniqueName extends HasName

  trait HasDescription {

    /** A description that can provide additional details on the name. */
    val description: String

  }

  /** A trait that can be used to define the properties for an entity.
    */
  trait HasOptionalDescription {

    /** An optional description that can provide additional details on the name. */
    val description: Option[String]

  }

  trait HasNamePredicates[A <: HasName] {

    type EntityNameFilter = A => Boolean

    val nameIsOneOf: Set[String] => EntityNameFilter =
      names => entity => names.contains(entity.name)

    val nameIsLike: Set[String] => EntityNameFilter =
      names => entity => {
        val lc = entity.name.toLowerCase
        names.forall(n => lc.contains(n.toLowerCase))
      }

  }

  trait HasStatePredicates[A <: HasState] {

    type EntityStateFilter = A => Boolean

    val stateIsOneOf: Set[EntityState] => EntityStateFilter =
      states => entity => states.contains(entity.state)

  }

}

// move package object here due to: https://issues.scala-lang.org/browse/SI-9922
package object domain {

  /** Used to validate commands received by the system that act on the domain model. */
  type DomainValidation[A] = ValidationNel[DomainError, A]

  /** Contains an error messsage when an invalid condition happens. */
  type DomainError = String

}
