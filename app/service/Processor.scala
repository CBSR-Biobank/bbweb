package service

import Messages._
import domain._
import akka.actor._
import org.slf4j.Logger
import Messages._

import scalaz._
import Scalaz._

trait Processor extends Actor with ActorLogging {

  protected def process[T](
    serviceMsg: ServiceMsg,
    validation: DomainValidation[T]) = {
    logCommand("", serviceMsg)
    validation.foreach { event =>
      //listeners sendEvent event
    }
    logEvent(validation)
    sender ! validation
  }

  private def logEvent[T](validation: DomainValidation[T]) {
    if (log.isDebugEnabled) {
      validation match {
        case Success(item) =>
          log.debug("%s".format(item))
        case Failure(msglist) =>
          log.debug("msg: %s".format(msglist.head))
      }
    }
  }

  private def logCommand[T](processorName: String, cmd: Any) {
    if (log.isDebugEnabled) {
      log.debug("%s: %s".format(processorName, cmd))
    }
  }

  private def logCommand[T](processorName: String, serviceMsg: ServiceMsg) {
    if (log.isDebugEnabled) {
      log.debug("%s:\n\t{cmd: %s,\n\tuserId: %s,\n\tid: %s }".format(processorName,
        serviceMsg.cmd, serviceMsg.userId, serviceMsg.id.getOrElse("NONE")))
    }
  }
}