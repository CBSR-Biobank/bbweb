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
    (__ \ "type").read[String](Reads.verifying[String](_ == "RegisterUserCmd")) andKeep
      (__ \ "name").read[String](minLength[String](2)) and
      (__ \ "email").read[String](minLength[String](5)) and
      (__ \ "password").read[String](minLength[String](2)) and
      (__ \ "avatarUrl").readNullable[String](minLength[String](2))
  )((name, email, password, avatarUrl) => RegisterUserCmd(name, email, password, avatarUrl))

  implicit val activateUserCmdReads = (
    (__ \ "type").read[String](Reads.verifying[String](_ == "ActivateUserCmd")) andKeep
      (__ \ "expectedVersion").readNullable[Long](min[Long](0)) and
      (__ \ "email").read[String](minLength[String](5))
  )((version, email) => ActivateUserCmd(version, email))

  implicit val updateUserCmdReads = (
    (__ \ "type").read[String](Reads.verifying[String](_ == "UpdateUserCmd")) andKeep
      (__ \ "expectedVersion").readNullable[Long](min[Long](0)) and
      (__ \ "name").read[String](minLength[String](2)) and
      (__ \ "email").read[String](minLength[String](5)) and
      (__ \ "password").read[String](minLength[String](2)) and
      (__ \ "avatarUrl").readNullable[String](minLength[String](2))
  )((version, name, email, password, avatarUrl) => UpdateUserCmd(version, name, email, password, avatarUrl))

  implicit val lockUserCmdReads = (
    (__ \ "type").read[String](Reads.verifying[String](_ == "LockUserCmd")) andKeep
      (__ \ "expectedVersion").readNullable[Long](min[Long](0)) and
      (__ \ "email").read[String](minLength[String](5))
  )((version, email) => LockUserCmd(version, email))

  implicit val unlockUserCmdReads = (
    (__ \ "type").read[String](Reads.verifying[String](_ == "UnlockUserCmd")) andKeep
      (__ \ "expectedVersion").readNullable[Long](min[Long](0)) and
      (__ \ "email").read[String](minLength[String](5))
  )((version, email) => UnlockUserCmd(version, email))

  implicit val removeUserCmdReads = (
    (__ \ "type").read[String](Reads.verifying[String](_ == "RemoveUserCmd")) andKeep
      (__ \ "expectedVersion").readNullable[Long](min[Long](0)) and
      (__ \ "email").read[String](minLength[String](5))
  )((version, email) => RemoveUserCmd(version, email))

}
