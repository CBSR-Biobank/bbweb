package org.biobank.domain.user

import org.biobank.domain.IdentifiedValueObject

import play.api.libs.json._

final case class UserId(id: String) extends IdentifiedValueObject[String] {}

object UserId {

  implicit val userIdWriter: Writes[UserId] = Writes{ (userId: UserId) => JsString(userId.id) }

}
