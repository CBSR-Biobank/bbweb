package fixture

import infrastructure._
import service._
import domain._
import domain.study._
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.stm.Ref
import akka.actor._
import akka.util.Timeout
import org.eligosource.eventsourced.core._
import org.eligosource.eventsourced.journal.mongodb.casbah.MongodbCasbahJournalProps
import scalaz._
import Scalaz._
import service.study.StudyService

abstract class StudyFixture extends AppFixture {

  lazy val studyProcessor = extension.processorOf(Props(
    new StudyProcessor() with Emitter with Eventsourced { val id = 1 }))

  lazy val studyService = new StudyService(studyProcessor)
}