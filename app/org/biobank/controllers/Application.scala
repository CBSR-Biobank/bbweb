package org.biobank.controllers

import scala.language.postfixOps
import play.api.mvc._
import views._
import securesocial.core.SecureSocial

/**
 * Controller for the main page, and also the about and contact us pages.
 */
object Application extends Controller with SecureSocial {

  def index = SecuredAction { implicit request =>
    Ok(html.index(request))
  }

  def about = UserAwareAction { implicit request =>
    Ok(html.about())
  }

  def contact = UserAwareAction { implicit request =>
    Ok(html.contact())
  }

}

