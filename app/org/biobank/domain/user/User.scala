package org.biobank.domain.user

import org.biobank.ValidationKey
import org.biobank.infrastructure.JsonUtils._
import org.biobank.domain._
import org.biobank.infrastructure.EnumUtils._
import org.joda.time.DateTime
import play.api.libs.json._
import scala.util.matching.Regex
import scalaz.Scalaz._

/**
 * Predicates that can be used to filter collections of users.
 *
 */
trait UserPredicates extends HasNamePredicates[User] {

  type UserFilter = User => Boolean

  val emailIsOneOf: Set[String] => UserFilter =
    emails => user => emails.contains(user.email)

  val emailContains: String => UserFilter =
    email => entity => entity.email.contains(email.replaceAll("[\\*]", ""))

}

/**
 * A user of the system.
 */
sealed trait User extends ConcurrencySafeEntity[UserId] with HasState with HasName {

  /** the user's current state */
  val state: EntityState

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

  /**
   * Authenticate a user.
   */
  def authenticate(email: String, password: String): DomainValidation[User] = {
    if (this.password == password) this.successNel[String]
    else DomainError("authentication failure").failureNel[User]
  }

  override def toString: String =
    s"""|${this.getClass.getSimpleName}: {
        |  id: $id,
        |  version: $version,
        |  timeAdded: $timeAdded,
        |  timeModified: $timeModified,
        |  state: $state,
        |  name: $name,
        |  email: $email,
        |  password: $password,
        |  salt: $salt,
        |  avatarUrl: $avatarUrl,
        |}""".stripMargin
}

object User {

  val registeredState: EntityState = new EntityState("registered")
  val activeState: EntityState = new EntityState("active")
  val lockedState: EntityState = new EntityState("locked")

  val userStates: List[EntityState] = List(registeredState,
                                           activeState,
                                           lockedState)

  @SuppressWarnings(Array("org.wartremover.warts.Option2Iterable"))
  implicit val userFormat: Format[User] = new Format[User] {
    override def writes(user: User): JsValue = {
      ConcurrencySafeEntity.toJson(user) ++
      Json.obj("state"    -> user.state.id,
               "name"     -> user.name,
               "email"    -> user.email,
               "password" -> user.password,
               "salt"     -> user.salt) ++
      JsObject(
        Seq[(String, JsValue)]() ++
          user.avatarUrl.map("avatarUrl" -> Json.toJson(_)))
    }

      override def reads(json: JsValue): JsResult[User] = (json \ "state") match {
          case JsDefined(JsString(registeredState.id)) => json.validate[RegisteredUser]
          case JsDefined(JsString(activeState.id)) => json.validate[ActiveUser]
          case JsDefined(JsString(lockedState.id)) => json.validate[LockedUser]
          case _ => JsError("error")
        }
  }

  implicit val registeredUserReads: Reads[RegisteredUser] = Json.reads[RegisteredUser]
  implicit val activeUserReads: Reads[ActiveUser]         = Json.reads[ActiveUser]
  implicit val lockedUserReads: Reads[LockedUser]         = Json.reads[LockedUser]

  val sort2Compare: Map[String, (User, User) => Boolean] =
    Map[String, (User, User) => Boolean](
      "name"  -> compareByName,
      "email" -> compareByEmail,
      "state" -> compareByState)

  // users with duplicate emails are not allowed
  def compareByEmail(a: User, b: User): Boolean =
    (a.email compareToIgnoreCase b.email) < 0

  def compareByName(a: User, b: User): Boolean = {
    val nameCompare = a.name compareToIgnoreCase b.name
    if (nameCompare == 0) compareByEmail(a, b)
    else nameCompare < 0
  }

  def compareByState(a: User, b: User): Boolean = {
    val stateCompare = a.state.toString compare b.state.toString
    if (stateCompare == 0) compareByName(a, b)
    else stateCompare < 0
  }
}

trait UserValidations {
  val NameMinLength: Long = 2L

  case object PasswordRequired extends ValidationKey

  case object SaltRequired extends ValidationKey

  case object InvalidName extends ValidationKey

  case object InvalidEmail extends ValidationKey

  case object InvalidUrl extends ValidationKey

  val emailRegex: Regex =
    "[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?".r

  val urlRegex: Regex =
    "^((https?|ftp)://|(www|ftp)\\.)[a-z0-9-]+(\\.[a-z0-9-]+)+([/?].*)?$".r

  def validateEmail(email: String): DomainValidation[String] = {
    emailRegex.findFirstIn(email).fold { InvalidEmail.failureNel[String] } { e => email.successNel }
  }

  def validateAvatarUrl(urlOption: Option[String]): DomainValidation[Option[String]] = {
    urlOption.fold {
      none[String].successNel[String]
    } { url  =>
      urlRegex.findFirstIn(url).fold {
        InvalidUrl.failureNel[Option[String]]
      } { e =>
        some(url).successNel[String]
      }
    }
  }
}


/** A user that just registered with the system. This user does not yet have full access
  * the system.
  */
final case class RegisteredUser(id:           UserId,
                                version:      Long,
                                timeAdded:    DateTime,
                                timeModified: Option[DateTime],
                                name:         String,
                                email:        String,
                                password:     String,
                                salt:         String,
                                avatarUrl:    Option[String])
    extends { val state: EntityState = new EntityState("registered") }
    with User
    with UserValidations {

  /* Activates a registered user. */
  def activate(): DomainValidation[ActiveUser] = {
    ActiveUser(id           = this.id,
               version      = this.version + 1,
               timeAdded    = this.timeAdded,
               timeModified = Some(DateTime.now),
               name         = this.name,
               email        = this.email,
               password     = this.password,
               salt         = this.salt,
               avatarUrl    = this.avatarUrl).successNel[String]
  }

}

/** Factory object. */
object RegisteredUser extends UserValidations {
  import CommonValidations._

  /** Creates a registered user. */
  def create(id:        UserId,
             version:   Long,
             name:      String,
             email:     String,
             password:  String,
             salt:      String,
             avatarUrl: Option[String]): DomainValidation[RegisteredUser] = {

    (validateId(id) |@|
       validateVersion(version) |@|
       validateString(name, NameMinLength, InvalidName) |@|
       validateEmail(email) |@|
       validateString(password, PasswordRequired) |@|
       validateString(salt, SaltRequired) |@|
       validateAvatarUrl(avatarUrl)) {
      case (_, _, _, _, _, _, _) =>
        RegisteredUser(id, version, DateTime.now, None, name, email, password, salt, avatarUrl)
      }
  }

}

/** A user that has access to the system. */
final case class ActiveUser(id:           UserId,
                            version:      Long,
                            timeAdded:    DateTime,
                            timeModified: Option[DateTime],
                            name:         String,
                            email:        String,
                            password:     String,
                            salt:         String,
                            avatarUrl:    Option[String])
    extends { val state: EntityState = new EntityState("active") }
    with User
    with UserValidations {
  import CommonValidations._

  def withName(name: String): DomainValidation[ActiveUser] = {
    validateString(name, NameMinLength, InvalidName).map( _ =>
      copy(name         = name,
           version      = version + 1,
           timeModified = Some(DateTime.now)))
  }

  def withEmail(email: String): DomainValidation[ActiveUser] = {
    validateEmail(email).map(_ =>
      copy(email        = email,
           version      = version + 1,
           timeModified = Some(DateTime.now)))
  }

  def withPassword(password: String, salt: String): DomainValidation[ActiveUser] = {
    validateString(password, PasswordRequired).map(_ =>
      copy(password     = password,
           salt         = salt,
           version      = version + 1,
           timeModified = Some(DateTime.now)))
  }

  def withAvatarUrl(avatarUrl: Option[String]): DomainValidation[ActiveUser] = {
    validateAvatarUrl(avatarUrl).map(_ =>
      copy(avatarUrl = avatarUrl,
           version = version + 1,
           timeModified = Some(DateTime.now)))
  }

  /** Locks an active user. */
  def lock(): DomainValidation[LockedUser] = {
    LockedUser(id           = this.id,
               version      = this.version + 1,
               timeAdded    = this.timeAdded,
               timeModified = Some(DateTime.now),
               name         = this.name,
               email        = this.email,
               password     = this.password,
               salt         = this.salt,
               avatarUrl    = this.avatarUrl).successNel[String]
  }

}

/** A user who no longer has access to the system. */
final case class LockedUser(id:           UserId,
                            version:      Long,
                            timeAdded:    DateTime,
                            timeModified: Option[DateTime],
                            name:         String,
                            email:        String,
                            password:     String,
                            salt:         String,
                            avatarUrl:    Option[String])
    extends { val state: EntityState = new EntityState("locked") }
    with User {

  /** Unlocks a locked user. */
  def unlock(): DomainValidation[ActiveUser] = {
    ActiveUser(id           = this.id,
               version      = this.version + 1,
               timeAdded    = this.timeAdded,
               timeModified = Some(DateTime.now),
               name         = this.name,
               email        = this.email,
               password     = this.password,
               salt         = this.salt,
               avatarUrl    = this.avatarUrl).successNel[String]
  }

}

object UserHelper {
  import org.biobank.CommonValidations._

  def isUserRegistered(user: User): DomainValidation[RegisteredUser] = {
    user match {
      case registeredUser: RegisteredUser => registeredUser.successNel[String]
      case _ => InvalidStatus(s"not registered").failureNel[RegisteredUser]
    }
  }

  def isUserActive(user: User): DomainValidation[ActiveUser] = {
    user match {
      case activeUser: ActiveUser => activeUser.successNel[String]
      case _ => InvalidStatus(s"not active").failureNel[ActiveUser]
    }
  }

  def isUserLocked(user: User): DomainValidation[LockedUser] = {
    user match {
      case lockedUser: LockedUser => lockedUser.successNel[String]
      case _ => InvalidStatus(s"not active").failureNel[LockedUser]
    }
  }

  def isUserNotLocked(user: User): DomainValidation[User] = {
    user match {
      case lockedUser: LockedUser => InvalidStatus(s"user is locked").failureNel[User]
      case _ => user.successNel[String]
    }
  }
}
