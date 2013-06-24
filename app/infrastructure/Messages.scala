package infrastructure

import domain._

import org.eligosource.eventsourced.core.Message

trait HasCommand { val cmd: Any }
trait HasUserId { val userId: UserId }
trait HasIdentity { val id: String }

case class BiobankMsg(cmd: Any, userId: UserId) extends HasCommand with HasUserId
case class BiobankMsgWithId(cmd: Any, userId: UserId, id: String)
  extends HasCommand with HasUserId with HasIdentity
