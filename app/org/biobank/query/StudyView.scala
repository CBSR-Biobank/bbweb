package org.biobank.query

// Upgraded to Play 2.3-M1 and it does not yet have a Slick plugin.
//
// Commenting out code for now
//import play.api.db.slick._

//import org.biobank.query.model._
import org.biobank.infrastructure.event.StudyEventsJson._
import org.biobank.infrastructure.event.StudyEvents._
import org.biobank.service.WrappedEvent

import play.api.Play.current
import akka.persistence.PersistentView
import akka.actor.{ Actor, ActorLogging }
import org.slf4j.Logger

class StudyView extends PersistentView with ActorLogging {

  override def persistenceId: String = "study-processor-id"

  override def viewId: String = "study-view-id"

  def receive: Actor.Receive = {
    case event: StudyAddedEvent =>
    // DB.withSession { implicit s: Session =>
    //   Studies.insert(Study(event.id, 0L, event.name, event.description, false))
    //   log.debug("study added with event %s" format event)
    // }
    case event: StudyUpdatedEvent =>
    // DB.withSession { implicit s: Session =>
    //   Studies.update(Study(event.id, event.version, event.name, event.description, false))
    //   log.debug("study updated with event %s" format event)
    // }
    case msg =>
      log.debug("received event %s" format msg)
  }
}

