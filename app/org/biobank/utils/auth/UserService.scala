package org.biobank.utils.auth

import java.util.UUID
import javax.inject.Inject

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.services.IdentityService
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
import org.biobank.domain.users.UserId
import org.biobank.services.users.UsersService
import scala.concurrent.{ ExecutionContext, Future }

/**
 * Handles actions to users.
 */
trait UserService extends IdentityService[User] {
  /**
   * Retrieves a user that matches the specified ID.
   *
   * @param id The ID to retrieve a user.
   * @return The retrieved user or None if no user could be retrieved for the given ID.
   */
  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  def retrieve(id: UUID): Future[Option[User]]

  /**
   * Saves a user.
   *
   * @param user The user to save.
   * @return The saved user.
   */
  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  def save(user: User): Future[User]

  /**
   * Saves the social profile for a user.
   *
   * If a user exists for this profile then update the user, otherwise create a new user with the given profile.
   *
   * @param profile The social profile to save.
   * @return The user for whom the profile was saved.
   */
  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  def save(profile: CommonSocialProfile): Future[User]
}

/**
 * Handles actions to users.
 *
 * @param userDAO The user DAO implementation.
 */
 @SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
class UserServiceImpl @Inject() (usersService: UsersService,
                                 implicit val executionContext: ExecutionContext) extends UserService {

  def retrieve(id: UUID) = Future {
      usersService.getUser(UserId(id.toString)).toOption.map(User.apply)
    }

  def retrieve(loginInfo: LoginInfo): Future[Option[User]] =
    Future { usersService.getUser(UserId(loginInfo.providerKey)).toOption.map(User.apply) }

  def save(user: User) = ???

  def save(profile: CommonSocialProfile) = ???

}
