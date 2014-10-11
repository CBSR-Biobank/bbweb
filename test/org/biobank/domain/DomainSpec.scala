package org.biobank.domain

import org.biobank.fixture.TestComponentImpl
import org.biobank.infrastructure._

import akka.actor.ActorSystem
import akka.util.Timeout
import org.scalatest._
import scalaz._
import scalaz.Scalaz._

trait DomainSpec extends WordSpec with MustMatchers with TestComponentImpl {

  implicit override val system: ActorSystem = null

  override val studiesProcessor = null
  override val centresProcessor = null
  override val usersProcessor = null

  override val studiesService = null
  override val centresService = null
  override val usersService = null

  val factory = new Factory

}
