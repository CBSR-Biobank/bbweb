package service

import service.commands._
import domain.UserId
import domain.study.DisabledStudy

object Messages {

  trait CommandMsg { val cmd: Command }
  trait HasIdentityOption { val id: Option[String] }
  trait HasUserId { val userId: UserId }

  case class ServiceMsg(
    cmd: Command,
    userId: UserId,
    id: Option[String] = None)
    extends CommandMsg with HasIdentityOption

  case class ProcessorMsg(
    cmd: Command,
    userId: UserId,
    id: Option[String] = None)
    extends CommandMsg with HasIdentityOption

  case class StudyProcessorMsg(
    cmd: Command,
    study: DisabledStudy,
    id: Option[String] = None)
    extends CommandMsg with HasIdentityOption

}
