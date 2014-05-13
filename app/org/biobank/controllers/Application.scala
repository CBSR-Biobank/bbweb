package org.biobank.controllers

import scala.language.postfixOps
import play.api.mvc._
import views._

/**
 * Controller for the main page, and also the about and contact us pages.
 */
object Application extends Controller  {

  def index = Action {
    Ok(html.index())
  }

  def about = Action {
    Ok(html.about())
  }

  def contact = Action {
    Ok(html.contact())
  }

}

