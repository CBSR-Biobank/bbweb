package infrastructure

import domain._

import org.eligosource.eventsourced.core.MessageEmitter

trait HasCommand { val cmd: Any }
trait HasUserId { val userId: UserId }
trait HasIdentity { val id: String }

case class BiobankMsg(cmd: Any, userId: UserId, id: Option[String] = None)
  extends HasCommand with HasUserId with HasIdentity

case class ProcessorMsg(cmd: Any, userId: UserId, time: Long, listeners: MessageEmitter,
  id: Option[String] = None)
  extends HasCommand with HasUserId with HasIdentity