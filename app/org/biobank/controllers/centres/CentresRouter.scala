package org.biobank.controllers.centres

import javax.inject.Inject
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

class CentresRouter @Inject()(controller: CentresController) extends SimpleRouter {
  import CentreRouting._

  override def routes: Routes = {

    // --- CENTRE DTOs ---
    case GET(p"/names") =>
      // this action extracts parameters from the raw query string
      controller.listNames

    case POST(p"/locations") =>
      controller.searchLocations

    // --- CENTRES ---
    case GET(p"/counts") =>
      controller.centreCounts

    case GET(p"/")  =>
      // this action extracts parameters from the raw query string
      controller.list

    case GET(p"/${centreId(id)}")  =>
      controller.query(id)

    case POST(p"/")  =>
      controller.add

    case POST(p"/locations/${centreId(id)}") =>
      controller.addLocation(id)

    case POST(p"/locations/${centreId(centreId)}/$locationId") =>
      controller.updateLocation(centreId, locationId)

    case POST(p"/snapshot") =>
      controller.snapshot

    case POST(p"/studies/${centreId(centreId)}") =>
      controller.addStudy(centreId)

    case POST(p"/name/${centreId(id)}")  =>
      controller.updateName(id)

    case POST(p"/description/${centreId(id)}")  =>
      controller.updateDescription(id)

    case POST(p"/enable/${centreId(id)}")  =>
      controller.enable(id)

    case POST(p"/disable/${centreId(id)}")  =>
      controller.disable(id)

    case DELETE(p"/studies/${centreId(centreId)}/${long(ver)}/$studyId") =>
      controller.removeStudy(centreId, ver, studyId)

    case DELETE(p"/locations/${centreId(centreId)}/${long(ver)}/$locationId") =>
      controller.removeLocation(centreId, ver, locationId)

  }

}
