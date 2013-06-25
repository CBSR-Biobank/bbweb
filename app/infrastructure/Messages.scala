package infrastructure

import domain.UserId
import domain.study.DisabledStudy

import org.eligosource.eventsourced.core.MessageEmitter

trait HasCommand { val cmd: Any }
trait HasIdentityOption { val id: Option[String] }
trait HasUserId { val userId: UserId }

case class ServiceMsg(cmd: Any, userId: UserId, id: Option[String] = None)
  extends HasCommand with HasIdentityOption

case class ProcessorMsg(cmd: Any, listeners: MessageEmitter, id: Option[String] = None)
  extends HasCommand with HasIdentityOption

case class StudyProcessorMsg(cmd: Any, study: DisabledStudy,
  listeners: MessageEmitter, id: Option[String] = None)
  extends HasCommand with HasIdentityOption