package service

import Messages._
import domain._

import akka.actor._
import org.slf4j.Logger
import akka.persistence.EventsourcedProcessor

import scalaz._
import Scalaz._

trait Processor extends EventsourcedProcessor with ActorLogging {

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