package org.biobank.controllers.study

import javax.inject.Inject
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

class SpecimenLinkTypesRouter @Inject()(controller: SpecimenLinkTypesController) extends SimpleRouter {
  import StudiesRouting._

  override def routes: Routes = {

    case GET(p"/${procTypeId(ptId)}" ? q_o"cetId=${slTypeId(sltId)}") =>
      controller.get(ptId, sltId)

    case POST(p"/${procTypeId(ptId)}") =>
      controller.addSpecimenLinkType(ptId)

    case PUT(p"/${procTypeId(ptId)}/${slTypeId(sltId)}") =>
      controller.updateSpecimenLinkType(ptId, sltId)

    case DELETE(p"/${procTypeId(ptId)}/${slTypeId(sltId)}/${long(ver)}") =>
      controller.removeSpecimenLinkType(ptId, sltId, ver)

  }
}
