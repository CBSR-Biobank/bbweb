package org.biobank.controllers.centres

import javax.inject.Inject
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

class ShipmentsRouter @Inject()(controller: ShipmentsController) extends SimpleRouter {
  import CentreRouting._

  override def routes: Routes = {
    case POST(p"/") =>
      controller.add

    case GET(p"/list") =>
      controller.list

    case POST(p"/courier/${shipmentId(id)}") =>
      controller.updateCourier(id)

    case POST(p"/trackingnumber/${shipmentId(id)}") =>
      controller.updateTrackingNumber(id)

    case POST(p"/fromlocation/${shipmentId(id)}") =>
      controller.updateFromLocation(id)

    case POST(p"/tolocation/${shipmentId(id)}") =>
      controller.updateToLocation(id)

    case POST(p"/snapshot") =>
      controller.snapshot

    case POST(p"/state/created/${shipmentId(id)}") =>
      controller.created(id)

    case POST(p"/state/packed/${shipmentId(id)}") =>
      controller.packed(id)

    case POST(p"/state/sent/${shipmentId(id)}") =>
      controller.sent(id)

    case POST(p"/state/received/${shipmentId(id)}") =>
      controller.received(id)

    case POST(p"/state/unpacked/${shipmentId(id)}") =>
      controller.unpacked(id)

    case POST(p"/state/completed/${shipmentId(id)}") =>
      controller.completed(id)

    case POST(p"/state/lost/${shipmentId(id)}") =>
      controller.lost(id)

    case POST(p"/state/skip-to-sent/${shipmentId(id)}") =>
      controller.skipStateSent(id)

    case POST(p"/state/skip-to-unpacked/${shipmentId(id)}") =>
      controller.skipStateUnpacked(id)

    case GET(p"/specimens/canadd/${shipmentId(shId)}/$invId") =>
      controller.canAddSpecimens(shId, invId)

    case GET(p"/specimens/${shipmentId(shId)}/$shSpcId") =>
      controller.getSpecimen(shId, shSpcId)

    case GET(p"/specimens/${shipmentId(id)}") =>
      // this action extracts parameters from the query string
      controller.listSpecimens(id)

    case GET(p"/${shipmentId(id)}") =>
      controller.get(id)

    case POST(p"/specimens/${shipmentId(id)}") =>
      controller.addSpecimen(id)

    case POST(p"/specimens/container/${shipmentId(shId)}") =>
      controller.specimenContainer(shId)

    case POST(p"/specimens/present/${shipmentId(shId)}") =>
      controller.specimenPresent(shId)

    case POST(p"/specimens/received/${shipmentId(shId)}") =>
      controller.specimensReceived(shId)

    case POST(p"/specimens/missing/${shipmentId(shId)}") =>
      controller.specimensMissing(shId)

    case POST(p"/specimens/extra/${shipmentId(shId)}") =>
      controller.specimensExtra(shId)

    case DELETE(p"/${shipmentId(id)}/${long(ver)}") =>
      controller.remove(id, ver)

    case DELETE(p"/specimens/${shipmentId(shId)}/$shSpcId/${long(ver)}") =>
      controller.removeSpecimen(shId, shSpcId, ver)
  }

}
