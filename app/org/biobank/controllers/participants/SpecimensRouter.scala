package org.biobank.controllers.participants


import javax.inject.Inject
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

class SpecimensRouter @Inject()(controller: SpecimensController) extends SimpleRouter {
  import ParticipantsRouting._

  override def routes: Routes = {
    case GET(p"/get/${specimenId(spcId)}") =>
      controller.get(spcId)

    case GET(p"/invid/$invId") =>
      controller.getByInventoryId(invId)

    case GET(p"/${collectionEventId(ceId)}" ? q_o"sort=$sort"
               & q_o"page=${int(page)}"
               & q_o"limit=${int(limit)}"
               & q_o"order=$order") =>
      controller.list(ceId, sort, page, limit, order)

    case POST(p"/${collectionEventId(ceId)}") =>
      controller.addSpecimens(ceId)

    case DELETE(p"/${collectionEventId(ceId)}/${specimenId(spcId)}/${long(ver)}") =>
      controller.removeSpecimen(ceId, spcId, ver)

  }
}
