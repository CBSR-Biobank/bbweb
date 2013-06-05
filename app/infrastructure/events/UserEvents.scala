package infrastructure.events

import domain.UserId

case class UserAddedEvent(id: UserId, name: String, email: String)
