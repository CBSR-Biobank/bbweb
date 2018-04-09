package org.biobank.controllers.studies

import javax.inject.Inject
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

class CeventTypesRouter @Inject()(controller: CeventTypesController) extends SimpleRouter {
  import StudiesRouting._
  import org.biobank.controllers.SlugRouting._

  override def routes: Routes = {

    case GET(p"/inuse/${slug(s)}") =>
      controller.inUse(s)

    case GET(p"/names/${slug(s)}") =>
      controller.listNames(s)

    case GET(p"/${slug(studySlug)}/${slug(ceventTypeSlug)}") =>
      controller.get(studySlug, ceventTypeSlug)

    case GET(p"/${slug(s)}") =>
      controller.list(s)

    case POST(p"/snapshot") =>
      controller.snapshot

    case POST(p"/name/${ceventTypeId(cetId)}") =>
      controller.updateName(cetId)

    case POST(p"/description/${ceventTypeId(cetId)}") =>
      controller.updateDescription(cetId)

    case POST(p"/recurring/${ceventTypeId(cetId)}") =>
      controller.updateRecurring(cetId)

    case POST(p"/spcdesc/${ceventTypeId(cetId)}") =>
      controller.addSpecimenDefinition(cetId)

    case POST(p"/spcdesc/${ceventTypeId(cetId)}/$sdId") =>
      controller.updateSpecimenDefinition(cetId, sdId)

    case DELETE(p"/spcdesc/${studyId(id)}/${ceventTypeId(cetId)}/${long(ver)}/$sdId") =>
      controller.removeSpecimenDefinition(id, cetId, ver, sdId)

    case POST(p"/annottype/${ceventTypeId(cetId)}") =>
      controller.addAnnotationType(cetId)

    case POST(p"/annottype/${ceventTypeId(cetId)}/$annotationTypeId") =>
      controller.updateAnnotationType(cetId, annotationTypeId)

    case POST(p"/${studyId(id)}") =>
      controller.add(id)

    case DELETE(p"/${studyId(id)}/${ceventTypeId(cetId)}/${long(ver)}") =>
      controller.remove(id, cetId, ver)

    case DELETE(p"/annottype/${studyId(id)}/${ceventTypeId(cetId)}/${long(ver)}/$annotationTypeId") =>
      controller.removeAnnotationType(id, cetId, ver, annotationTypeId)

  }

}
