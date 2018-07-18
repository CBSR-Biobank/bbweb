package org.biobank.utils.auth

import com.mohiva.play.silhouette.api.Env
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator

/**
 * The default env.
 */
trait DefaultEnv extends Env {
  type I = User
  type A = JWTAuthenticator
}
