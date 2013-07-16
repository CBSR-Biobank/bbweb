package service

import infrastructure.commands._
import domain.UserId
import domain.study.DisabledStudy

import org.eligosource.eventsourced.core.MessageEmitter

trait HasCommand { val cmd: Command }
trait HasIdentityOption { val id: Option[String] }
trait HasUserId { val userId: UserId }

case class ServiceMsg(cmd: Command, userId: UserId, id: Option[String] = None)
  extends HasCommand with HasIdentityOption

case class ProcessorMsg(cmd: Command, listeners: MessageEmitter, id: Option[String] = None)
  extends HasCommand with HasIdentityOption

case class StudyProcessorMsg(cmd: Command, study: DisabledStudy,
  listeners: MessageEmitter, id: Option[String] = None)
  extends HasCommand with HasIdentityOption