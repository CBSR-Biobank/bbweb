package org.biobank.controllers.studies

import javax.inject.Inject
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

class ProcessingTypesRouter @Inject()(controller: ProcessingTypesController) extends SimpleRouter {
  import org.biobank.controllers.SlugRouting._
  import StudiesRouting._

  override def routes: Routes = {

    case GET(p"/${slug(studySlug)}/${slug(procTypeSlug)}") =>
      controller.get(studySlug, procTypeSlug)

    case POST(p"/snapshot") =>
      controller.snapshot

    case POST(p"/${studyId(sId)}") =>
      controller.addProcessingType(sId)

    case PUT(p"/${studyId(sId)}/${procTypeId(ptId)}") =>
      controller.updateProcessingType(sId, ptId)

    case DELETE(p"/${studyId(sId)}/${procTypeId(ptId)}/${long(ver)}") =>
      controller.removeProcessingType(sId, ptId, ver)

  }
}
