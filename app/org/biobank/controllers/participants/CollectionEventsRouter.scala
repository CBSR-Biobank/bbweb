package org.biobank.controllers.participants

import javax.inject.Inject
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

class CollectionEventsRouter @Inject()(controller: CollectionEventsController) extends SimpleRouter {
  import ParticipantsRouting._

  override def routes: Routes = {

    case GET(p"/list/${participantId(id)}" ? q_o"sort=$sort"
               & q_o"page=${int(page)}"
               & q_o"limit=${int(limit)}"
               & q_o"order=$order") =>
      controller.list(id, sort, page, limit, order)

    case GET(p"/visitNumber/${participantId(id)}/${int(vn)}") =>
      controller.getByVisitNumber(id, vn)

    case GET(p"/$collectionEventId") =>
      controller.get(collectionEventId)

    case POST(p"/${participantId(id)}") =>
      controller.add(id)

    case POST(p"/visitNumber/${collectionEventId(id)}") =>
      controller.updateVisitNumber(id)

    case POST(p"/timeCompleted/${collectionEventId(id)}") =>
      controller.updateTimeCompleted(id)

    case POST(p"/annot/${collectionEventId(id)}") =>
      controller.addAnnotation(id)

    case DELETE(p"/annot/${collectionEventId(id)}/$annotTypeId/${long(ver)}") =>
      controller.removeAnnotation(id, annotTypeId, ver)

    case DELETE(p"/${participantId(pId)}/${collectionEventId(ceId)}/${long(ver)}") =>
      controller.remove(pId, ceId, ver)

  }
}
