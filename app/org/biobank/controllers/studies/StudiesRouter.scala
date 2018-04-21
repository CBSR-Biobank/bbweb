package org.biobank.controllers.studies

import javax.inject.Inject
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

class StudiesRouter @Inject()(controller: StudiesController) extends SimpleRouter {
  import StudiesRouting._
  import org.biobank.controllers.SlugRouting._

  override def routes: Routes = {

    case GET(p"/collectionStudies") =>
      // this action extracts parameters from the raw query string
      controller.collectionStudies

    case GET(p"/names") =>
      // this action extracts parameters from the raw query string
      controller.listNames

    case GET(p"/counts") =>
      controller.studyCounts

    case GET(p"/valuetypes") =>
      controller.valueTypes

    case GET(p"/enableAllowed/${studyId(id)}") =>
      controller.enableAllowed(id)

    case GET(p"/anatomicalsrctypes") =>
      controller.anatomicalSourceTypes

    case GET(p"/specimentypes") =>
      controller.specimenTypes

    case GET(p"/preservtypes") =>
      controller.preservTypes

    case GET(p"/preservtemptypes") =>
      controller.preservTempTypes

    case GET(p"/sgvaluetypes") =>
      controller.specimenDefinitionValueTypes

    case GET(p"/search") =>
      // this action extracts parameters from the raw query string
      controller.list

    case GET(p"/${slug(s)}") =>
      controller.getBySlug(s)

    case POST(p"/snapshot") =>
      controller.snapshot

    case POST(p"/") =>
      controller.add

    case POST(p"/name/${studyId(id)}") =>
      controller.updateName(id)

    case POST(p"/description/${studyId(id)}") =>
      controller.updateDescription(id)

    case POST(p"/pannottype/${studyId(id)}") =>
      controller.addAnnotationType(id)

    case POST(p"/pannottype/${studyId(id)}/$annotationTypeId") =>
      controller.updateAnnotationType(id, annotationTypeId)

    case DELETE(p"/pannottype/${studyId(id)}/${long(ver)}/$annotationTypeId") =>
      controller.removeAnnotationType(id, ver, annotationTypeId)

    case POST(p"/enable/${studyId(id)}") =>
      controller.enable(id)

    case POST(p"/disable/${studyId(id)}") =>
      controller.disable(id)

    case POST(p"/retire/${studyId(id)}") =>
      controller.retire(id)

    case POST(p"/unretire/${studyId(id)}") =>
      controller.unretire(id)

    case GET(p"/centres/${studyId(id)}") =>
      controller.centresForStudy(id)

  }
}
