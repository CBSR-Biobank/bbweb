package org.biobank.domain

import org.biobank.fixture.TestComponentImpl
import org.biobank.infrastructure._

import akka.actor.ActorSystem
import akka.util.Timeout
import org.scalatest.WordSpecLike
import org.scalatest.Matchers
import scalaz._
import scalaz.Scalaz._

trait DomainSpec extends TestComponentImpl with WordSpecLike with Matchers {

  implicit override val system: ActorSystem = null

  override val studiesProcessor = null
  override val centresProcessor = null
  override val usersProcessor = null

  override val studiesService = null
  override val centresService = null
  override val usersService = null

  val factory = new Factory

}
