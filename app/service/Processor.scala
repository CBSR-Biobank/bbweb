package service

import domain._
import akka.actor._
import org.slf4j.Logger
import org.eligosource.eventsourced.core.Message

import scalaz._
import Scalaz._

trait Processor extends Actor with ActorLogging {

  protected def process[T](validation: DomainValidation[T]) = {
    sender ! validation
  }

  protected def logMethod[T](
    methodName: String,
    cmd: Any,
    validation: DomainValidation[T]) {
    if (log.isDebugEnabled) {
      log.debug("%s: %s".format(methodName, cmd))
      validation match {
        case Success(item) =>
          log.debug("%s: %s".format(methodName, item))
        case Failure(msglist) =>
          log.debug("%s: { msg: %s }".format(methodName, msglist.head))
      }
    }
  }
}