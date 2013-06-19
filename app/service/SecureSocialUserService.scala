/**
 * Copyright 2012 Jorge Aliss (jaliss at gmail dot com) - twitter: @jaliss
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
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
    if (Logger.isDebugEnabled) {
      Logger.debug("find { user: %s }".format(user.getOrElse("")))
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
