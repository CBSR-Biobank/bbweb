package fixture

import org.biobank.infrastructure._
import org.biobank.service._
import org.biobank.service.study._
import org.biobank.domain._
import org.biobank.domain.study._
import org.biobank.query.model._

import play.api.Mode
import play.api.Mode._
import akka.actor._
import scala.concurrent._
import org.specs2.mutable._
import org.specs2.time._

//import scala.slick.session.Database
//import scala.slick.session.Database
//import Database.threadLocalSession
//import scala.slick.jdbc.{ GetResult, StaticQuery => Q }

import scalaz._
import Scalaz._

/**
 * Used to test the study query model.
 */
trait StudyQueryFixture extends TestFixture {

  //  protected val DB = Database.forURL("jdbc:h2:mem:bbweb-test;MODE=MYSQL", driver = "org.h2.Driver")
  //
  //  val studyView = system.actorOf(Props(new StudyViewImpl), "studyview")

  override val studyProcessor = null
  override val userProcessor = null

  override val studyService = null
  override val userService = null
}