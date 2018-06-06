package org.biobank.controllers.studies

import javax.inject.Inject
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

class ProcessingTypesRouter @Inject()(controller: ProcessingTypesController) extends SimpleRouter {
  import org.biobank.controllers.SlugRouting._
  import StudiesRouting._

  override def routes: Routes = {

    case GET(p"/id/${studyId(sId)}/${procTypeId(ptId)}") =>
      controller.getById(sId, ptId)

    case GET(p"/inuse/${slug(s)}") =>
      controller.inUse(s)

    case GET(p"/spcdefs/${studyId(sId)}") =>
      controller.specimenDefinitions(sId)

    case GET(p"/${slug(studySlug)}/${slug(procTypeSlug)}") =>
      controller.get(studySlug, procTypeSlug)

    case GET(p"/${slug(studySlug)}") =>
      controller.list(studySlug)

    case POST(p"/snapshot") =>
      controller.snapshot

    case POST(p"/${studyId(sId)}") =>
      controller.addProcessingType(sId)

    case POST(p"/update/${studyId(sId)}/${procTypeId(ptId)}") =>
      controller.update(sId, ptId)

    case POST(p"/annottype/${procTypeId(ptId)}") =>
      controller.addAnnotationType(ptId)

    case POST(p"/annottype/${procTypeId(ptId)}/$annotationTypeId") =>
      controller.updateAnnotationType(ptId, annotationTypeId)

    case DELETE(p"/${studyId(sId)}/${procTypeId(ptId)}/${long(ver)}") =>
      controller.removeProcessingType(sId, ptId, ver)

    case DELETE(p"/annottype/${studyId(id)}/${procTypeId(ptId)}/${long(ver)}/$annotationTypeId") =>
      controller.removeAnnotationType(id, ptId, ver, annotationTypeId)
  }
}
