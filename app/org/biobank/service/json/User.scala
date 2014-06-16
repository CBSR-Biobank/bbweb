package org.biobank.service.json

import org.biobank.domain._
import org.biobank.infrastructure.command.UserCommands._

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import org.joda.time.DateTime
import scala.collection.immutable.Map

object User {
  import JsonUtils._

  implicit val userIdWriter = Writes{ (userId: UserId) => JsString(userId.id) }

  implicit val userWrites = new Writes[User] {
    def writes(user: User) = Json.obj(
      "id"             -> user.id,
      "version"        -> user.version,
      "addedDate"      -> user.addedDate,
      "lastUpdateDate" -> user.lastUpdateDate,
      "name"           -> user.name,
      "email"          -> user.email,
      "avatarUrl"      -> user.avatarUrl
    )
  }

  implicit val registerUserCmdReads = (
    (__ \ "name").read[String](minLength[String](2)) and
      (__ \ "email").read[String](minLength[String](5)) and
      (__ \ "password").read[String](minLength[String](2)) and
      (__ \ "avatarUrl").readNullable[String](minLength[String](2))
  )(RegisterUserCmd.apply _)

  implicit val activateUserCmdReads = (
    (__ \ "expectedVersion").readNullable[Long](min[Long](0)) and
      (__ \ "email").read[String](minLength[String](5))
  )(ActivateUserCmd.apply _)

  implicit val updateUserCmdReads = (
    (__ \ "expectedVersion").readNullable[Long](min[Long](0)) and
      (__ \ "name").read[String](minLength[String](2)) and
      (__ \ "email").read[String](minLength[String](5)) and
      (__ \ "password").read[String](minLength[String](2)) and
      (__ \ "avatarUrl").readNullable[String](minLength[String](2))
  )(UpdateUserCmd.apply _)

  implicit val lockUserCmdReads = (
    (__ \ "expectedVersion").readNullable[Long](min[Long](0)) and
      (__ \ "email").read[String](minLength[String](5))
  )(LockUserCmd.apply _)

  implicit val unlockUserCmdReads = (
    (__ \ "expectedVersion").readNullable[Long](min[Long](0)) and
      (__ \ "email").read[String](minLength[String](5))
  )(UnlockUserCmd.apply _)

  implicit val removeUserCmdReads = (
    (__ \ "expectedVersion").readNullable[Long](min[Long](0)) and
      (__ \ "email").read[String](minLength[String](5))
  )(RemoveUserCmd.apply _)

}
