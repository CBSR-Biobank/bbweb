package org.biobank.service

import org.biobank.infrastructure.command.Commands._
import org.biobank.domain.user.UserId

import akka.actor.ActorRef
import akka.util.Timeout
import akka.pattern.AskSupport
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

trait ApplicationService {

//  implicit val timeout = Timeout(5 seconds)

  def ask[T <: Command]
    (processor: ActorRef, command: T, userIdOpt: Option[UserId])
    (implicit timeout: Timeout)
      : Future[_] = {
    akka.pattern.ask(processor, WrappedCommand(command, userIdOpt))
  }

  def ask[T <: Command]
    (processor: ActorRef, command: T, userId: UserId)
    (implicit timeout: Timeout)
      : Future[_] = {
    ask(processor, command, Some(userId))
  }

  def ask[T <: Command]
    (processor: ActorRef, command: T)
    (implicit timeout: Timeout)
      : Future[_] = {
    ask(processor, command, None)
  }

}
