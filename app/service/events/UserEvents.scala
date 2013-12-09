package service.events

import domain.UserId

object UserEvents {

  case class UserAddedEvent(id: UserId, name: String, email: String)
  case class UserAuthenticatedEvent(id: UserId, name: String, email: String)

}
