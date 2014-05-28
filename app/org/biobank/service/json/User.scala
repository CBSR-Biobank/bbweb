package org.biobank.service.json

import org.biobank.domain._

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import org.joda.time.DateTime
import scala.collection.immutable.Map

object User {

  implicit val userWrites = new Writes[User] {
    def writes(user: User) = Json.obj(
      "name"           -> user.name,
      "email"          -> user.email,
      "avatarUrl"      -> user.avatarUrl,
      "addedDate"      -> user.addedDate,
      "lastUpdateDate" -> user.lastUpdateDate
    )
  }

}
