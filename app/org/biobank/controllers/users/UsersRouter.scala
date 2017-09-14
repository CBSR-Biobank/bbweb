package org.biobank.controllers.users

import org.biobank.domain.user.UserId
import javax.inject.Inject
import play.api.mvc.PathBindable.Parsing
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

class UsersRouter @Inject()(controller: UsersController) extends SimpleRouter {
  import UsersRouting._

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

    case GET(p"/") =>
      // this action extracts parameters from the query string
      controller.list

    case GET(p"/${userId(id)}") =>
      controller.user(id)

    case POST(p"/name/${userId(id)}") =>
      controller.updateName(id)

    case POST(p"/email/${userId(id)}") =>
      controller.updateEmail(id)

    case POST(p"/password/${userId(id)}") =>
      controller.updatePassword(id)

    case POST(p"/avatarurl/${userId(id)}") =>
      controller.updateAvatarUrl(id)

    case POST(p"/activate/${userId(id)}") =>
      controller.activateUser(id)

    case POST(p"/lock/${userId(id)}") =>
      controller.lockUser(id)

    case POST(p"/unlock/${userId(id)}") =>
      controller.unlockUser(id)

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
