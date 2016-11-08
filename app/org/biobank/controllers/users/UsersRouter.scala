package org.biobank.controllers.users

import org.biobank.domain.user.UserId
import javax.inject.Inject
import play.api.mvc.PathBindable.Parsing
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

class UsersRouter @Inject()(controller: UsersController) extends SimpleRouter {

  implicit object bindableUserId extends Parsing[UserId](
    UserId.apply,
    _.id,
    (key: String, e: Exception) => s"$key is not a valid user Id"
  )

  val userId = new PathBindableExtractor[UserId]

  override def routes: Routes = {

    case GET(p"/authenticate") =>
      controller.authenticateUser

    case GET(p"/counts") =>
      controller.userCounts()

    case GET(p"/studies/${userId(id)}" ?  q_o"query=$query"
               & q_o"sort=$sort"
               & q_o"order=$order") =>
      controller.userStudies(id, query, sort, order)

    case GET(p"/" ? q_o"nameFilter=$nameFilter"
               & q_o"emailFilter=$emailFilter"
               & q_o"status=$status"
               & q_o"sort=$sort"
               & q_o"page=${int(page)}"
               & q_o"limit=${int(limit)}"
               & q_o"order=$order") =>
      controller.list(nameFilter, emailFilter, status, sort, page, limit, order)

    case GET(p"/${userId(id)}") =>
      controller.user(id)

    case POST(p"/") =>
      controller.registerUser

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
  }
}
