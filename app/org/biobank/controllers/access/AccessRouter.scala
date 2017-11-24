package org.biobank.controllers.access

import org.biobank.domain.access._
import javax.inject.Inject
import play.api.mvc.PathBindable.Parsing
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

class AccessRouter @Inject()(controller: AccessController) extends SimpleRouter {
  import AccessItemRouting._
  //import PermissionsRouting._
  import MembershipsRouting._
  import org.biobank.controllers.study.StudiesRouting._
  import org.biobank.controllers.centres.CentreRouting._
  import org.biobank.controllers.users.UsersRouting._

  override def routes: Routes = {

    case GET(p"/items/names") =>
      // this action extracts parameters from the raw query string
      controller.listItemNames

    case GET(p"/roles") =>
      // this action extracts parameters from the query string
      controller.listRoles

    case GET(p"/roles/names") =>
      // this action extracts parameters from the raw query string
      controller.listRoleNames

    case GET(p"/roles/${accessItemId(id)}") =>
      // this action extracts parameters from the query string
      controller.getRole(id)

    case GET(p"/memberships") =>
      controller.listMemberships

    case GET(p"/memberships/${membershipId(mId)}") =>
      controller.getMembership(mId)

    case POST(p"/roles") =>
      controller.roleAdd

    case POST(p"/roles/name/${accessItemId(rId)}") =>
      controller.roleUpdateName(rId)

    case POST(p"/roles/description/${accessItemId(rId)}") =>
      controller.roleUpdateDescription(rId)

    case POST(p"/roles/user/${accessItemId(rId)}") =>
      controller.roleAddUser(rId)

    case POST(p"/roles/parent/${accessItemId(rId)}") =>
      controller.roleAddParent(rId)

    case POST(p"/roles/child/${accessItemId(rId)}") =>
      controller.roleAddChild(rId)

    case POST(p"/memberships") =>
      controller.membershipAdd

    case POST(p"/memberships/name/${membershipId(mId)}") =>
      controller.membershipUpdateName(mId)

    case POST(p"/memberships/description/${membershipId(mId)}") =>
      controller.membershipUpdateDescription(mId)

    case POST(p"/memberships/user/${membershipId(mId)}") =>
      controller.membershipAddUser(mId)

    case POST(p"/memberships/allStudies/${membershipId(mId)}") =>
      controller.membershipAllStudies(mId)

    case POST(p"/memberships/study/${membershipId(mId)}") =>
      controller.membershipAddStudy(mId)

    case POST(p"/memberships/allCentres/${membershipId(mId)}") =>
      controller.membershipAllCentres(mId)

    case POST(p"/memberships/centre/${membershipId(mId)}") =>
      controller.membershipAddCentre(mId)

    case DELETE(p"/roles/user/${accessItemId(rId)}/${long(ver)}/${userId(uId)}") =>
      controller.roleRemoveUser(rId, ver, uId)

    case DELETE(p"/roles/parent/${accessItemId(rId)}/${long(ver)}/${accessItemId(pId)}") =>
      controller.roleRemoveParent(rId, ver, pId)

    case DELETE(p"/roles/child/${accessItemId(rId)}/${long(ver)}/${accessItemId(cId)}") =>
      controller.roleRemoveChild(rId, ver, cId)

    case DELETE(p"/roles/${accessItemId(rId)}/${long(ver)}") =>
      controller.roleRemove(rId, ver)

    case DELETE(p"/memberships/${membershipId(mId)}/${long(ver)}") =>
      controller.membershipRemove(mId, ver)

    case DELETE(p"/memberships/user/${membershipId(mId)}/${long(ver)}/${userId(uId)}") =>
      controller.membershipRemoveUser(mId, ver, uId)

    case DELETE(p"/memberships/study/${membershipId(mId)}/${long(ver)}/${studyId(sId)}") =>
      controller.membershipRemoveStudy(mId, ver, sId)

    case DELETE(p"/memberships/centre/${membershipId(mId)}/${long(ver)}/${centreId(cId)}") =>
      controller.membershipRemoveCentre(mId, ver, cId)

  }
}

object AccessItemRouting {

  implicit object bindableAccessItemId extends Parsing[AccessItemId](
    AccessItemId.apply,
    _.toString,
    (key: String, e: Exception) => s"$key is not a valid access item id"
  )

  val accessItemId: PathBindableExtractor[AccessItemId] =
    new PathBindableExtractor[AccessItemId]

}


object MembershipsRouting {

  implicit object bindableMembershipId extends Parsing[MembershipId](
    MembershipId.apply,
    _.toString,
    (key: String, e: Exception) => s"$key is not a valid membership id"
  )

  val membershipId: PathBindableExtractor[MembershipId] =
    new PathBindableExtractor[MembershipId]

}
