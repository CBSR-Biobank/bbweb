package controllers

import service.AppServices

import play.api._
import play.api.mvc.Results._
import play.api.mvc.RequestHeader

object Global extends GlobalSettings {

  lazy val services: AppServices = AppServices.boot

  override def beforeStart(app: Application) {
    services
  }

  override def onBadRequest(request: RequestHeader, error: String) = {
    BadRequest("Bad Request: " + error)
  }

}
