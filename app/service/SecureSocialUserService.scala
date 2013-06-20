package service

import play.api.{ Logger, Application }
import securesocial.core._
import securesocial.core.providers.Token
import securesocial.core.UserId
import scala.Some
import org.mindrot.jbcrypt.BCrypt

/**
 *
 */
class SecureSocialUserService(application: Application) extends UserServicePlugin(application) {

  private lazy val userService = controllers.Global.services.userService
  private var tokens = Map[String, Token]()

  def find(id: UserId): Option[Identity] = {
    val user = userService.find(id)
    if (Logger.isTraceEnabled) {
      Logger.trace("find { user: %s }".format(user.getOrElse("")))
    }
    user map { _.asInstanceOf[Identity] }
  }

  def findByEmailAndProvider(email: String, providerId: String): Option[Identity] = {
    userService.findByEmailAndProvider(email, providerId)
  }

  def save(user: Identity): Identity = {
    userService.add(user)
  }

  def save(token: Token) {
    tokens += (token.uuid -> token)
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
