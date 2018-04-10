package org.biobank.domain.users

import com.google.inject.ImplementedBy
import javax.inject.{Inject, Singleton}
import org.biobank.{Global, TestData}
import org.biobank.domain._
import org.slf4j.{Logger, LoggerFactory}
import play.api.{Configuration, Environment, Mode}
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

/** A repository that stores [[User]]s. */
@ImplementedBy(classOf[UserRepositoryImpl])
trait UserRepository extends ReadWriteRepositoryWithSlug[UserId, User] {

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
class UserRepositoryImpl @Inject() (val config:   Configuration,
                                    val env:      Environment,
                                    val testData: TestData)
    extends ReadWriteRepositoryRefImplWithSlug[UserId, User](v => v.id)
    with UserRepository {
  import org.biobank.CommonValidations._

  val log: Logger = LoggerFactory.getLogger(this.getClass)

  override def init(): Unit = {
    super.init()
    createDefaultUser
    testData.testUsers.foreach(put)
    testData.accessUsers.foreach(put)
  }

  def nextIdentity: UserId = new UserId(nextIdentityAsString)

  protected def notFound(id: UserId): IdNotFound = IdNotFound(s"user id: $id")

  protected def slugNotFound(slug: String): EntityCriteriaNotFound =
    EntityCriteriaNotFound(s"user slug: $slug")

  def allUsers(): Set[User] = getValues.toSet

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
    val name = "Administrator"
    val adminEmail =
      if (env.mode == Mode.Dev) org.biobank.Global.DefaultUserEmail
      else config.get[Option[String]]("admin.email").getOrElse(org.biobank.Global.DefaultUserEmail)

    put(ActiveUser(id           = org.biobank.Global.DefaultUserId,
                   version      = 0L,
                   timeAdded    = Global.StartOfTime,
                   timeModified = None,
                   slug         = Slug(name),
                   name         = name,
                   email        = adminEmail,
                   password     = "$2a$10$Kvl/h8KVhreNDiiOd0XiB.0nut7rysaLcKpbalteFuDN8uIwaojCa",
                   salt         = "$2a$10$Kvl/h8KVhreNDiiOd0XiB.",
                   avatarUrl    = None))
  }
}
