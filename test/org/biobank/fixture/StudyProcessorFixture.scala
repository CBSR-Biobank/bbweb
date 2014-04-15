package org.biobank.fixture

import org.biobank.infrastructure._
import org.biobank.service._
import org.biobank.domain._
import org.biobank.domain.study._

import play.api.Mode
import play.api.Mode._
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.stm.Ref
import akka.actor._
import akka.persistence.View
import akka.util.Timeout
import org.specs2.mutable._
import org.specs2.time._

import scalaz._
import Scalaz._

/**
 * Used to test the study service.
 */
trait StudyProcessorFixture extends TestFixture {

  override val studyProcessor = system.actorOf(Props(new StudyProcessorImpl), "studyproc")
  override val userProcessor = null

  override val studyService = null
  override val userService = null
}
