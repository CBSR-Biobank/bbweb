package org.biobank.controllers.users

import org.biobank.domain.users.UserId
import javax.inject.Inject
import play.api.mvc.PathBindable.Parsing
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

class UsersRouter @Inject()(controller: UsersController) extends SimpleRouter {
  import UsersRouting._
  import org.biobank.controllers.access.AccessItemRouting._
  import org.biobank.controllers.access.MembershipRouting._
  import org.biobank.controllers.SlugRouting._

  override def routes: Routes = {

    case GET(p"/names") =>
      // this action extracts parameters from the raw query string
      controller.listNames

    case GET(p"/authenticate") =>
      controller.authenticateUser

    case GET(p"/counts") =>
      controller.userCounts()

    case GET(p"/studies") =>
      // this action extracts parameters from the raw query string
      controller.userStudies

    case GET(p"/search") =>
      // this action extracts parameters from the query string
      controller.list

    case GET(p"/${slug(s)}") =>
      controller.getBySlug(s)

    case POST(p"/update/${userId(id)}") =>
      controller.update(id)

    case POST(p"/roles/${userId(id)}") =>
      controller.addRole(id)

    case POST(p"/memberships/${userId(id)}") =>
      controller.addMembership(id)

    case POST(p"/login") =>
      controller.login

    case POST(p"/logout") =>
      controller.logout

    case POST(p"/passreset") =>
      controller.passwordReset

    case POST(p"/snapshot") =>
      controller.snapshot

    case POST(p"/") =>
      controller.registerUser

    case DELETE(p"/roles/${userId(uId)}/${long(ver)}/${accessItemId(rId)}") =>
      controller.removeRole(uId, ver, rId)

    case DELETE(p"/memberships/${userId(uId)}/${long(ver)}/${membershipId(rId)}") =>
      controller.removeMembership(uId, ver, rId)

  }
}

object UsersRouting {

  implicit object bindableUserId extends Parsing[UserId](
    UserId.apply,
    _.id,
    (key: String, e: Exception) => s"$key is not a valid user Id"
  )

  val userId: PathBindableExtractor[UserId] = new PathBindableExtractor[UserId]

}
