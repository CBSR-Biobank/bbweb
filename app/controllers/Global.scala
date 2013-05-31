package controllers

import play.api._
import play.api.mvc.Results._
import service._
import service.AppServices._
import play.api.mvc.RequestHeader
import views.html.defaultpages.notFound

object Global extends GlobalSettings {

  lazy val services: AppServices = boot

}