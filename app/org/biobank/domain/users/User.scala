package org.biobank.domain.users

import java.time.OffsetDateTime
import org.biobank.ValidationKey
import org.biobank.domain._
import play.api.libs.json._
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

  val emailIsLike: Set[String] => UserFilter =
    emails => entity => {
      val lc = entity.email.toLowerCase
      emails.forall(e => lc.contains(e.toLowerCase))
    }

}

/**
 * A user of the system.
 */
sealed trait User extends ConcurrencySafeEntity[UserId] with HasState with HasUniqueName with HasSlug {

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
  def authenticate(password: String): DomainValidation[User] = {
    if (this.password == password) this.successNel[String]
    else DomainError("authentication failure").failureNel[User]
  }

  override def toString: String =
    s"""|${this.getClass.getSimpleName}: {
        |  id:           $id,
        |  version:      $version,
        |  timeAdded:    $timeAdded,
        |  timeModified: $timeModified,
        |  state:        $state,
        |  slug:         $slug,
        |  name:         $name,
        |  email:        $email,
        |  password:     $password,
        |  salt:         $salt,
        |  avatarUrl:    $avatarUrl,
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
               "slug"     -> user.slug,
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

  case object SaltRequired extends ValidationKey

  case object InvalidName extends ValidationKey

}


/** A user that just registered with the system. This user does not yet have full access
  * the system.
  */
final case class RegisteredUser(id:           UserId,
                                version:      Long,
                                timeAdded:    OffsetDateTime,
                                timeModified: Option[OffsetDateTime],
                                slug:         Slug,
                                name:         String,
                                email:        String,
                                password:     String,
                                salt:         String,
                                avatarUrl:    Option[String])
    extends { val state: EntityState = new EntityState("registered") }
    with User
    with UserValidations {

  /* if registration is valid, the user can be activated and allowed to access the system */
  def activate(): DomainValidation[ActiveUser] = {
    ActiveUser(id           = this.id,
               version      = this.version + 1,
               timeAdded    = this.timeAdded,
               timeModified = Some(OffsetDateTime.now),
               slug         = this.slug,
               name         = this.name,
               email        = this.email,
               password     = this.password,
               salt         = this.salt,
               avatarUrl    = this.avatarUrl).successNel[String]
  }

  /* if registration is invalid, the user registration is locked and not allowed to register again */
  def lock(): DomainValidation[LockedUser] = {
    LockedUser(id           = this.id,
               version      = this.version + 1,
               timeAdded    = this.timeAdded,
               timeModified = Some(OffsetDateTime.now),
               slug         = this.slug,
               name         = this.name,
               email        = this.email,
               password     = this.password,
               salt         = this.salt,
               avatarUrl    = this.avatarUrl).successNel[String]
  }



}

/** Factory object. */
object RegisteredUser extends UserValidations {
  import org.biobank.CommonValidations._
  import org.biobank.domain.DomainValidations._

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
       validateNonEmptyString(name, InvalidName) |@|
       validateName(name) |@|
       validateEmail(email) |@|
       validateNonEmptyString(password, PasswordRequired) |@|
       validateNonEmptyString(salt, SaltRequired) |@|
       validateOptionalUrl(avatarUrl)) { case _ =>
        RegisteredUser(id           = id,
                       version      = version,
                       timeAdded    = OffsetDateTime.now,
                       timeModified = None,
                       slug         = Slug(name),
                       name         = name,
                       email        = email,
                       password     = password,
                       salt         = salt,
                       avatarUrl    = avatarUrl)
    }
  }

}

/** A user that has access to the system. */
final case class ActiveUser(id:           UserId,
                            version:      Long,
                            timeAdded:    OffsetDateTime,
                            timeModified: Option[OffsetDateTime],
                            slug: Slug,
                            name:         String,
                            email:        String,
                            password:     String,
                            salt:         String,
                            avatarUrl:    Option[String])
    extends { val state: EntityState = new EntityState("active") }
    with User
    with UserValidations {
  import org.biobank.CommonValidations._
  import org.biobank.domain.DomainValidations._

  def withName(name: String): DomainValidation[ActiveUser] = {
    (validateNonEmptyString(name) |@|
       validateName(name)) { case _ =>
        copy(name         = name,
             version      = version + 1,
             timeModified = Some(OffsetDateTime.now))
    }
  }

  def withEmail(email: String): DomainValidation[ActiveUser] = {
    validateEmail(email).map(_ =>
      copy(email        = email,
           version      = version + 1,
           timeModified = Some(OffsetDateTime.now)))
  }

  def withPassword(password: String, salt: String): DomainValidation[ActiveUser] = {
    (validateNonEmptyString(password, PasswordRequired) |@|
       validateNonEmptyString(salt, SaltRequired)) { case _ =>
        copy(password     = password,
             salt         = salt,
             version      = version + 1,
             timeModified = Some(OffsetDateTime.now))
    }
  }

  def withAvatarUrl(avatarUrl: Option[String]): DomainValidation[ActiveUser] = {
    val validAvatarUrl = avatarUrl match {
        case Some(url) => validateUrl(url)
        case None      => "valid".successNel[String]
      }
    validAvatarUrl.map(_ =>
      copy(avatarUrl    = avatarUrl,
           version      = version + 1,
           timeModified = Some(OffsetDateTime.now)))
  }

  /** A registered user that can no longer access the system should be locked */
  def lock(): DomainValidation[LockedUser] = {
    LockedUser(id           = this.id,
               version      = this.version + 1,
               timeAdded    = this.timeAdded,
               timeModified = Some(OffsetDateTime.now),
               slug         = this.slug,
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
                            timeAdded:    OffsetDateTime,
                            timeModified: Option[OffsetDateTime],
                            slug: Slug,
                            name:         String,
                            email:        String,
                            password:     String,
                            salt:         String,
                            avatarUrl:    Option[String])
    extends { val state: EntityState = new EntityState("locked") }
    with User {

  /** Unlocks a locked user */
  def unlock(): DomainValidation[ActiveUser] = {
    ActiveUser(id           = this.id,
               version      = this.version + 1,
               timeAdded    = this.timeAdded,
               timeModified = Some(OffsetDateTime.now),
               slug         = this.slug,
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
