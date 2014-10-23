package org.biobank.domain.user

import org.biobank.domain.{
  CommonValidations,
  ConcurrencySafeEntity,
  DomainValidation,
  DomainError,
  ValidationKey }
import org.biobank.infrastructure.event.UserEvents._
//import com.github.nscala_time.time.Imports._
import org.joda.time.DateTime
import org.biobank.infrastructure.JsonUtils._

import play.api.libs.json._
import play.api.libs.functional.syntax._
import scalaz._
import scalaz.Scalaz._

/** A user of the system.
  */
sealed trait User extends ConcurrencySafeEntity[UserId] {

  /** The user's full name. */
  val name: String

  /** The user's email. Must be unique to the system. */
  val email: String

  /** The user's password */
  val password: String

  /** The string used to salt the password. */
  val salt: String

  /** An optional URL to the user's avatar icon. */
  val avatarUrl: Option[String]

  /** Contains the current state of the object, one of: Registered, Active, Locked. */
  val status: String

  /**
   * Authenticate a user.
   */
  def authenticate(email: String, password: String): DomainValidation[User] = {
    if (this.password.equals(password)) this.success
    else DomainError("authentication failure").failNel
  }

  override def toString =
    s"""|${this.getClass.getSimpleName}: {
        |  id: $id,
        |  version: $version,
        |  timeAdded: $timeAdded,
        |  timeModified: $timeModified,
        |  name: $name,
        |  email: $email,
        |  password: $password,
        |  salt: $salt,
        |  avatarUrl: $avatarUrl,
        |  status: $status
        |}""".stripMargin
}

object User {

  implicit val userWrites = new Writes[User] {
    def writes(user: User) = Json.obj(
      "id"           -> user.id,
      "version"      -> user.version,
      "timeAdded"    -> user.timeAdded,
      "timeModified" -> user.timeModified,
      "name"         -> user.name,
      "email"        -> user.email,
      "avatarUrl"    -> user.avatarUrl,
      "status"       -> user.status
    )
  }

}

trait UserValidations {

  case object PasswordRequired extends ValidationKey

  case object SaltRequired extends ValidationKey

  case object InvalidEmail extends ValidationKey

  case object InvalidUrl extends ValidationKey

  val emailRegex = "[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?".r
  val urlRegex = "^((https?|ftp)://|(www|ftp)\\.)[a-z0-9-]+(\\.[a-z0-9-]+)+([/?].*)?$".r

  def validateEmail(email: String): DomainValidation[String] = {
    emailRegex.findFirstIn(email).fold { InvalidEmail.toString.failNel[String] } { e => email.successNel }
  }

  def validateAvatarUrl(urlOption: Option[String]): DomainValidation[Option[String]] = {
    urlOption.fold {
      none[String].successNel[String]
    } { url  =>
      urlRegex.findFirstIn(url).fold {
        InvalidUrl.toString.failNel[Option[String]]
      } { e =>
        some(url).successNel
      }
    }
  }
}


/** A user that just registered with the system. This user does not yet have full access
  * the system.
  */
case class RegisteredUser (
  id: UserId,
  version: Long,
  timeAdded: DateTime,
  timeModified: Option[DateTime],
  name: String,
  email: String,
  password: String,
  salt: String,
  avatarUrl: Option[String]) extends User with UserValidations {
  import CommonValidations._

  override val status: String = "Registered"

  /* Activates a registered user. */
  def activate: DomainValidation[ActiveUser] = {
    ActiveUser.create(this)
  }
}

/** Factory object. */
object RegisteredUser extends UserValidations {
  import CommonValidations._

  /** Creates a registered user. */
  def create(
    id: UserId,
    version: Long,
    dateTime: DateTime,
    name: String,
    email: String,
    password: String,
    salt: String,
    avatarUrl: Option[String]): DomainValidation[RegisteredUser] = {

    (validateId(id) |@|
      validateAndIncrementVersion(version) |@|
      validateString(name, NameRequired) |@|
      validateEmail(email) |@|
      validateString(password, PasswordRequired) |@|
      validateString(salt, SaltRequired) |@|
      validateAvatarUrl(avatarUrl)) {
        RegisteredUser(_, _, dateTime, None, _, _, _, _, _)
      }
  }

}

/** A user that has access to the system. */
case class ActiveUser (
  id: UserId,
  version: Long = -1,
  timeAdded: DateTime,
  timeModified: Option[DateTime],
  name: String,
  email: String,
  password: String,
  salt: String,
  avatarUrl: Option[String])
    extends User
    with UserValidations {
  import CommonValidations._

  override val status: String = "Active"

  def updateName(name: String): DomainValidation[ActiveUser] = {
    validateString(name, NameRequired).fold(
      err => err.failure,
      x => copy(version = version + 1, name = x).success
    )
  }

  def updateEmail(email: String): DomainValidation[ActiveUser] = {
    validateEmail(email).fold(
      err => err.failure,
      x => copy(version = version + 1, email = x).success
    )
  }

  def updatePassword(password: String, salt: String): DomainValidation[ActiveUser] = {
    validateString(password, PasswordRequired).fold(
      err => err.failure,
      pwd => copy(version = version + 1, password = pwd, salt = salt).success
    )
  }

  /** Locks an active user. */
  def lock: DomainValidation[LockedUser] = {
    LockedUser.create(this)
  }
}

/** Factory object. */
object ActiveUser extends UserValidations {
  import CommonValidations._

  /** Creates an active user from a registered user. */
  def create[T <: User](user: T): DomainValidation[ActiveUser] = {
    (validateId(user.id) |@|
      validateAndIncrementVersion(user.version) |@|
      validateString(user.name, NameRequired) |@|
      validateEmail(user.email) |@|
      validateString(user.password, PasswordRequired) |@|
      validateString(user.salt, SaltRequired) |@|
      validateAvatarUrl(user.avatarUrl)) {
        ActiveUser(_, _, user.timeAdded, None, _, _, _, _, _)
      }
  }

}

/** A user who no longer has access to the system. */
case class LockedUser (
  id: UserId,
  version: Long = -1,
  timeAdded: DateTime,
  timeModified: Option[DateTime],
  name: String,
  email: String,
  password: String,
  salt: String,
  avatarUrl: Option[String]) extends User {

  override val status: String = "Locked"

  /** Unlocks a locked user. */
  def unlock: DomainValidation[ActiveUser] = {
    ActiveUser.create(this)
  }

}

/** Factory object. */
object LockedUser extends UserValidations {
  import CommonValidations._

  /** Creates an active user from a locked user. */
  def create(user: ActiveUser): DomainValidation[LockedUser] = {
    (validateId(user.id) |@|
      validateAndIncrementVersion(user.version) |@|
      validateString(user.name, NameRequired) |@|
      validateEmail(user.email) |@|
      validateString(user.password, PasswordRequired) |@|
      validateString(user.salt, SaltRequired) |@|
      validateAvatarUrl(user.avatarUrl)) {
        LockedUser(_, _, user.timeAdded, None, _, _, _, _, _)
      }
  }

}

object UserHelper {

  def isUserRegistered(user: User): DomainValidation[RegisteredUser] = {
    user match {
      case registeredUser: RegisteredUser => registeredUser.success
      case _ => DomainError(s"the user is not registered").failNel
    }
  }

  def isUserActive(user: User): DomainValidation[ActiveUser] = {
    user match {
      case activeUser: ActiveUser => activeUser.success
      case _ => DomainError(s"the user is not active").failNel
    }
  }

  def isUserLocked(user: User): DomainValidation[LockedUser] = {
    user match {
      case lockedUser: LockedUser => lockedUser.success
      case _ => DomainError(s"the user is not active").failNel
    }
  }

  def isUserNotLocked(user: User): DomainValidation[User] = {
    user match {
      case lockedUser: LockedUser => DomainError(s"the user is locked").failNel
      case _ => user.success
    }
  }
}
