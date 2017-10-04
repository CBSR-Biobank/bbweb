package org.biobank.controllers

import javax.inject.Inject
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

class ApplicationRouter @Inject()(controller: Application) extends SimpleRouter {

  override def routes: Routes = {

    case GET(p"/api/dtos/counts") =>
      controller.aggregateCounts

    case GET(p"/") =>
      controller.index

  }
}
