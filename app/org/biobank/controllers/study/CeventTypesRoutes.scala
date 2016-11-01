package org.biobank.controllers.study

import javax.inject.Inject
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

class CeventTypesRouter @Inject()(controller: CeventTypesController) extends SimpleRouter {
  import StudiesRouting._

  override def routes: Routes = {

    case GET(p"/${studyId(id)}" ? q_o"cetId=${ceventTypeId(cetId)}") =>
      controller.get(id, cetId)

    case POST(p"/${studyId(id)}") =>
      controller.add(id)

    case DELETE(p"/${studyId(id)}/${ceventTypeId(cetId)}/${long(ver)}") =>
      controller.remove(id, cetId, ver)

    case GET(p"/inuse/${ceventTypeId(cetId)}") =>
      controller.inUse(cetId)

    case POST(p"/name/${ceventTypeId(cetId)}") =>
      controller.updateName(cetId)

    case POST(p"/description/${ceventTypeId(cetId)}") =>
      controller.updateDescription(cetId)

    case POST(p"/recurring/${ceventTypeId(cetId)}") =>
      controller.updateRecurring(cetId)

    case POST(p"/spcspec/${ceventTypeId(cetId)}") =>
      controller.addSpecimenSpec(cetId)

    case POST(p"/spcspec/${ceventTypeId(cetId)}/$uniqueId") =>
      controller.updateSpecimenSpec(cetId, uniqueId)

    case DELETE(p"/spcspec/${studyId(id)}/${ceventTypeId(cetId)}/${long(ver)}/$uniqueId") =>
      controller.removeSpecimenSpec(id, cetId, ver, uniqueId)

    case POST(p"/annottype/${ceventTypeId(cetId)}") =>
      controller.addAnnotationType(cetId)

    case POST(p"/annottype/${ceventTypeId(cetId)}/$uniqueId") =>
      controller.updateAnnotationType(cetId, uniqueId)

    case DELETE(p"/annottype/${studyId(id)}/${ceventTypeId(cetId)}/${long(ver)}/$uniqueId") =>
      controller.removeAnnotationType(id, cetId, ver, uniqueId)

  }

}
