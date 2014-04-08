package service

import domain._
import domain.study._

import play.api.Mode
import play.api.Mode._
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.util.Timeout
import akka.actor.Props
import akka.persistence._
import play.api.Logger

import play.api.Logger

object Configuration {
  val EventBusChannelId = 1
}

/**
 * Uses the Scala Cake Pattern to configure the application.
 */
trait TopComponent extends ServiceComponent {

  val studyProcessor: ActorRef
  val userProcessor: ActorRef

  val studyService: StudyService
  val userService: UserService

}

/**
 * Web Application Eventsourced configuration
 *
 * ==Akka-Persistence configuration==
 *
 *
 * ==Recovery==
 *
 * By default, recovery is only done on the Journal associated with the command processor to only
 * rebuild the ''In Memory Image''. To rebuild the ''query database'', the application can be run
 * with the `bbweb.query.db.load` system property set to `true` and the event processor will also
 * be recovered.
 *
 * @author Nelson Loyola
 */
trait TopComponentImpl extends TopComponent with ServiceComponentImpl {

  private implicit val system = ActorSystem("bbweb")

  val studyProcessor = system.actorOf(Props[StudyProcessor], "studyproc")
  val userProcessor = system.actorOf(Props[UserProcessor], "userproc")

  override val studyService = new StudyServiceImpl(studyProcessor)
  override val userService = new UserServiceImpl(userProcessor)
}
