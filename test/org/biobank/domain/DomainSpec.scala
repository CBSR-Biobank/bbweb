package org.biobank.domain

import org.scalatest._

trait DomainSpec extends WordSpecLike with MustMatchers {

  // TODO: can this be replaced with an Injectable singleton?
  val factory = new Factory

}

trait DomainFreeSpec extends FreeSpecLike {

  // TODO: can this be replaced with an Injectable singleton?
  val factory = new Factory

}
