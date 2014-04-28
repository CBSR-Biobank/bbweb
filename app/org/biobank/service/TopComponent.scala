package org.biobank.service

import org.biobank.domain._
import org.biobank.domain.study._

import akka.actor.{ ActorRef, ActorSystem }
import akka.util.Timeout
import akka.actor.Props
import akka.persistence._

/**
 * Uses the Scala Cake Pattern to configure the application.
 */
trait TopComponent extends ServiceComponent {

  implicit val system: ActorSystem

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

  override val studyProcessor = system.actorOf(Props(new StudyProcessorImpl), "studyproc")
  override val userProcessor = system.actorOf(Props(new UserProcessorImpl), "userproc")

  override val studyService = new StudyServiceImpl(studyProcessor)
  override val userService = new UserServiceImpl(userProcessor)
}
