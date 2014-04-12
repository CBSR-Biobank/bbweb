package org.biobank.service

import org.biobank.infrastructure.command.Commands._
import org.biobank.domain.UserId
import org.biobank.domain.study.DisabledStudy

object Messages {

  trait CommandMsg { val cmd: Command }
  trait HasIdentityOption { val id: Option[String] }
  trait HasUserId { val userId: UserId }

  // FIXME: remove this
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
