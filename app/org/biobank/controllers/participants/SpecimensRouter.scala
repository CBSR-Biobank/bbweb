package org.biobank.controllers.participants


import javax.inject.Inject
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

class SpecimensRouter @Inject()(controller: SpecimensController) extends SimpleRouter {
  import ParticipantsRouting._

  @SuppressWarnings(Array("org.wartremover.warts.PublicInference"))
  override def routes: Routes = {
    case GET(p"/get/${specimenId(spcId)}") =>
      controller.get(spcId)

    case GET(p"/invid/$invId") =>
      controller.getByInventoryId(invId)

    case GET(p"/${collectionEventId(ceId)}") =>
      // this action extracts parameters from the query string
      controller.list(ceId)

    case POST(p"/snapshot") =>
      controller.snapshot

    case POST(p"/${collectionEventId(ceId)}") =>
      controller.addSpecimens(ceId)

    case DELETE(p"/${collectionEventId(ceId)}/${specimenId(spcId)}/${long(ver)}") =>
      controller.removeSpecimen(ceId, spcId, ver)

  }
}
