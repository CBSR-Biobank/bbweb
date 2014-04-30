package org.biobank.service

import org.biobank.controllers.ApplicationComponent
import org.biobank.infrastructure.command.UserCommands._

import play.api.{ Logger, Application }
import securesocial.core._
import securesocial.core.providers.Token
import securesocial.core.IdentityId
import org.mindrot.jbcrypt.BCrypt
import scala.util.{ Failure, Success }
import scala.concurrent.ExecutionContext.Implicits.global

/**
 *
 */

class SecureSocialUserService(application: Application) extends UserServicePlugin(application) {

  lazy val userService = ApplicationComponent.userService

  private var tokens = Map[String, Token]()

  def find(id: IdentityId): Option[Identity] = {
    val user = userService.find(id)
    if (Logger.isDebugEnabled) {
      Logger.debug("find { user: %s }".format(user.getOrElse("")))
    }
    user map { _.asInstanceOf[Identity] }
  }

  def findByEmailAndProvider(email: String, providerId: String): Option[Identity] = {
    userService.findByEmailAndProvider(email, providerId)
  }

  def save(newUser: Identity): Identity = {
    newUser.passwordInfo match {
      case Some(passwordInfo) =>
        val cmd = RegisterUserCommand(newUser.fullName, newUser.email.getOrElse(""),
          passwordInfo.password, passwordInfo.hasher, passwordInfo.salt,
          newUser.avatarUrl)
        userService.add(cmd)
        newUser
      case None =>
        null
    }
  }

  def save(token: Token) {
    tokens += (token.uuid -> token)
  }

  /**
   * Links the current user Identity to another
   *
   * @param current The Identity of the current user
   * @param to The Identity that needs to be linked to the current user
   */
  def link(current: Identity, to: Identity) {

  }

  def findToken(token: String): Option[Token] = {
    tokens.get(token)
  }

  def deleteToken(uuid: String) {
    tokens -= uuid
  }

  def deleteTokens() {
    tokens = Map()
  }

  def deleteExpiredTokens() {
    tokens = tokens.filter(!_._2.isExpired)
  }
}
