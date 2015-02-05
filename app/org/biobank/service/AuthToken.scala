package org.biobank.service

import org.biobank.domain.{ DomainValidation, DomainError }
import org.biobank.domain.user.UserId

import play.api.Play.current
import play.api.cache.Cache
import scaldi.{Injectable, Injector}

import scalaz._
import Scalaz._
import scalaz.Validation.FlatMap._

/**
 *  Manages tokens used to authenticate logged in users.
 */
trait AuthToken {

  def newToken(userId: UserId): String

  def getUserId(token: String): DomainValidation[UserId]

}

class AuthTokenImpl extends AuthToken {

  val TokenExpirationSeconds =
    if (play.api.Play.current.mode == play.api.Mode.Prod) {
      60 * 15
    } else {
      60 * 60
    }

  /**
   *  Generates a new token for userId with an expiration of TokenExpirationSeconds.
   *
   *  TODO: Should token be derived from salt? not sure if required if server only runs HTTPS.
   */
  def newToken(userId: UserId): String = {
    val token = java.util.UUID.randomUUID.toString.replaceAll("-","")
    Cache.set(token, userId, TokenExpirationSeconds)
    token
  }

  /**
   *  If token is valid then the timeout is re-assigned on the cache.
   */
  def getUserId(token: String): DomainValidation[UserId] = {
    val maybeUserId = Cache.getAs[UserId](token)
      .map(_.success)
      .getOrElse(DomainError("invalid token").failureNel)
    maybeUserId map { userId => Cache.set(token, userId, TokenExpirationSeconds) }
    maybeUserId
  }

}
