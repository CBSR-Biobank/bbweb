package org.biobank.controllers.access

//import org.biobank.domain.access.RoleId
import javax.inject.Inject
//import play.api.mvc.PathBindable.Parsing
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

class AccessRouter @Inject()(controller: AccessController) extends SimpleRouter {

  // implicit object bindableRoleId extends Parsing[RoleId](
  //   RoleId.apply,
  //   _.id,
  //   (key: String, e: Exception) => s"$key is not a valid user Id"
  // )

  // val roleId: PathBindableExtractor[RoleId] = new PathBindableExtractor[RoleId]

  override def routes: Routes = {

    case GET(p"/roles") =>
      // this action extracts parameters from the query string
      controller.listRoles

  }
}
