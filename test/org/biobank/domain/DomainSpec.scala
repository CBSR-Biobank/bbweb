package org.biobank.domain

import org.biobank.fixture.TestComponentImpl
import org.biobank.infrastructure._
import org.biobank.fixture.NameGenerator

import akka.actor.ActorSystem
import akka.util.Timeout
import org.scalatest.WordSpecLike
import org.scalatest.Matchers
import scalaz._
import scalaz.Scalaz._

trait DomainSpec extends TestComponentImpl with FactoryComponent with WordSpecLike with Matchers {

  implicit override val system: ActorSystem = null

  override val studyProcessor = null
  override val userProcessor = null

  override val studyService = null
  override val userService = null


}
