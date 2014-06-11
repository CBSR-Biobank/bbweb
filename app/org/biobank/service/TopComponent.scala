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
 * @author Nelson Loyola
 */
trait TopComponentImpl extends TopComponent with ServiceComponentImpl {

}
