package controllers

import play.api._
import play.api.mvc.Results._
import play.api.mvc.RequestHeader

object WebComponent extends GlobalSettings with service.TopComponentImpl {

  override def beforeStart(app: Application) {
    start
    Logger.info("*** application started ***")
  }

  override def onBadRequest(request: RequestHeader, error: String) = {
    BadRequest("Bad Request: " + error)
  }

}
