package org.biobank.domain

import org.biobank.fixture.TestModule
import org.biobank.infrastructure._

import akka.actor.ActorSystem
import akka.util.Timeout
import org.scalatest._
import scaldi.akka.AkkaInjectable
import scalaz._
import scalaz.Scalaz._

trait DomainSpec extends WordSpec with MustMatchers with AkkaInjectable {

  implicit val appModule = new TestModule

  val factory = new Factory

}
