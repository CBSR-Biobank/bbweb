package org.biobank.domain.user

import com.google.inject.ImplementedBy
import javax.inject.Inject
import javax.inject.Singleton
import org.biobank.Global
import org.biobank.domain.{ DomainValidation, ReadWriteRepository, ReadWriteRepositoryRefImpl }
import org.slf4j.{Logger, LoggerFactory}
import play.api.{Configuration, Environment, Mode}
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

/** A repository that stores [[User]]s. */
@ImplementedBy(classOf[UserRepositoryImpl])
trait UserRepository extends ReadWriteRepository[UserId, User] {

  def allUsers(): Set[User]

  def getRegistered(id: UserId): DomainValidation[RegisteredUser]

  def getActive(id: UserId): DomainValidation[ActiveUser]

  def getLocked(id: UserId): DomainValidation[LockedUser]

  def getByEmail(email: String): DomainValidation[User]

}

/** An implementation of repository that stores [[User]]s.
 *
 * This repository uses the [[ReadWriteRepository]] implementation.
 */
@Singleton
class UserRepositoryImpl @Inject() (val config: Configuration,
                                    val env:    Environment)
    extends ReadWriteRepositoryRefImpl[UserId, User](v => v.id)
    with UserRepository {
  import org.biobank.CommonValidations._

  val log: Logger = LoggerFactory.getLogger(this.getClass)

  def nextIdentity: UserId = new UserId(nextIdentityAsString)

  def notFound(id: UserId): IdNotFound = IdNotFound(s"user id: $id")

  def allUsers(): Set[User] = getValues.toSet

  override def getByKey(id: UserId): DomainValidation[User] = {
    getMap.get(id).toSuccessNel(notFound(id).toString)
  }

  def getRegistered(id: UserId): DomainValidation[RegisteredUser] = {
    for {
      user <- getByKey(id)
      registered <- {
        user match {
          case u: RegisteredUser => u.successNel[String]
          case u => InvalidStatus(s"user is not registered: $id").failureNel[RegisteredUser]
        }
      }
    } yield registered
  }

  def getActive(id: UserId): DomainValidation[ActiveUser] = {
    for {
      user <- getByKey(id)
      active <- {
        user match {
          case u: ActiveUser => u.successNel[String]
          case u => InvalidStatus(s"user is not active: $id").failureNel[ActiveUser]
        }
      }
    } yield active
  }

  def getLocked(id: UserId): DomainValidation[LockedUser] = {
    for {
      user <- getByKey(id)
      locked <- {
        user match {
          case u: LockedUser => u.successNel[String]
          case u => InvalidStatus(s"user is not active: $id").failureNel[LockedUser]
        }
      }
    } yield locked
  }

  def getByEmail(email: String): DomainValidation[User] = {
    getValues.find(_.email == email)
      .toSuccess(EmailNotFound(s"user email not found: $email").nel)
  }

  /**
   * For new installations startup only:
   *
   * - password is "testuser"
   * - for production servers, the password should be changed as soon as possible
   */
  private def createDefaultUser(): Unit = {
    val adminEmail = if (env.mode == Mode.Dev) org.biobank.Global.DefaultUserEmail
                     else config.getString("admin.email").getOrElse(org.biobank.Global.DefaultUserEmail)

    if ((env.mode == Mode.Dev) || (env.mode == Mode.Prod)) {
      put(ActiveUser(id           = org.biobank.Global.DefaultUserId,
                     version      = 0L,
                     timeAdded    = Global.StartOfTime,
                     timeModified = None,
                     name         = "Administrator",
                     email        = adminEmail,
                     password     = "$2a$10$Kvl/h8KVhreNDiiOd0XiB.0nut7rysaLcKpbalteFuDN8uIwaojCa",
                     salt         = "$2a$10$Kvl/h8KVhreNDiiOd0XiB.",
                     avatarUrl    = None))
      log.info(s"created default user: $adminEmail")
    }
    ()
  }

  createDefaultUser
}
