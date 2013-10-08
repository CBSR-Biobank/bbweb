package query

import query.model._
import service.events._

import play.api.db.slick._
import play.api.Play.current
import akka.actor.Actor
import akka.actor.ActorLogging
import org.slf4j.Logger
import org.eligosource.eventsourced.core._

trait StudyEventProcessorComponent {

  trait StudyEventProcessor extends Actor with ActorLogging
}

trait StudyEventProcessorComponentImpl extends StudyEventProcessorComponent {

  class StudyEventProcessorImpl extends StudyEventProcessor {

    def receive = {
      case event: StudyAddedEvent =>
        DB.withSession { implicit s: Session =>
          Studies.insert(Study(event.id.id, event.name, event.description, false))
          log.debug("study added with event %s" format event)
        }
      case event: StudyUpdatedEvent =>
        DB.withSession { implicit s: Session =>
          Studies.update(Study(event.id.id, event.name, event.description, false))
          log.debug("study updated with event %s" format event)
        }
      case event =>
        log.debug("received event %s" format event)
    }
  }
}