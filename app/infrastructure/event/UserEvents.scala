package infrastructure.event

import infrastructure.command.UserCommands._

object UserEvents {

  sealed trait UserEvent

  case class UserAddedEvent(
    id: String,
    name: String,
    email: String,
    password: String,
    hasher: String,
    salt: Option[String],
    avatarUrl: Option[String])
    extends UserEvent

  case class UserAuthenticatedEvent(
    id: String,
    name: String,
    email: String)
    extends UserEvent

  object UserAddedEvent {

    def apply(cmd: AddUserCommand): UserAddedEvent = {
      UserAddedEvent(cmd.email, cmd.name, cmd.email, cmd.password, cmd.hasher, cmd.salt,
        cmd.avatarUrl)
    }

  }

}
