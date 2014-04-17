package org.biobank.service

import org.biobank.infrastructure.event.Events._
import org.biobank.domain._

import akka.actor._
import org.slf4j.Logger
import akka.persistence.EventsourcedProcessor

import scalaz._
import Scalaz._

trait Processor[K, A] extends EventsourcedProcessor with ActorLogging {

  protected val repository: ReadWriteRepository[K, A]

  protected def process[_](validation: DomainValidation[_]) {
      validation foreach { v =>
	val (event, obj) = v
	persist(event) { e =>
	  repository.put(obj)
	  sender ! event.success
	}
      }

      if (validation.isFailure) {
        sender ! validation
      }
    }

  protected def logEvent[T](validation: DomainValidation[T]) {
    if (log.isDebugEnabled) {
      validation match {
        case Success(item) =>
          log.debug("%s".format(item))
        case Failure(msglist) =>
          log.debug("msg: %s".format(msglist.head))
      }
    }
  }

  protected def logCommand[T](processorName: String, cmd: Any) {
    if (log.isDebugEnabled) {
      log.debug("%s: %s".format(processorName, cmd))
    }
  }
}
