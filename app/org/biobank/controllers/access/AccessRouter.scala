package org.biobank.controllers.access

import org.biobank.domain.access._
import org.biobank.domain.access.RoleId._
import org.biobank.domain.access.PermissionId._
import javax.inject.Inject
import play.api.mvc.PathBindable.Parsing
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

class AccessRouter @Inject()(controller: AccessController) extends SimpleRouter {
  import RolesRouting._
  //import PermissionsRouting._
  import MembershipsRouting._
  import org.biobank.controllers.study.StudiesRouting._
  import org.biobank.controllers.centres.CentreRouting._
  import org.biobank.controllers.users.UsersRouting._

  override def routes: Routes = {

    case GET(p"/roles") =>
      // this action extracts parameters from the query string
      controller.listRoles

    case GET(p"/roles/${roleId(id)}") =>
      // this action extracts parameters from the query string
      controller.getRole(id)

    case GET(p"/roles/permissions/${roleId(id)}") =>
      // this action extracts parameters from the query string
      controller.getRolePermissions(id)

    case GET(p"/memberships/${membershipId(mId)}") =>
      controller.getMembership(mId)

    case GET(p"/memberships") =>
      controller.listMemberships

    case POST(p"/memberships") =>
      controller.membershipAdd

    case DELETE(p"/memberships/${membershipId(mId)}/${long(ver)}") =>
      controller.membershipRemove(mId, ver)

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

    case DELETE(p"/memberships/user/${membershipId(mId)}/${long(ver)}/${userId(uId)}") =>
      controller.membershipRemoveUser(mId, ver, uId)

    case DELETE(p"/memberships/study/${membershipId(mId)}/${long(ver)}/${studyId(sId)}") =>
      controller.membershipRemoveStudy(mId, ver, sId)

    case DELETE(p"/memberships/centre/${membershipId(mId)}/${long(ver)}/${centreId(cId)}") =>
      controller.membershipRemoveCentre(mId, ver, cId)

  }
}

object RolesRouting {

  implicit object bindableRoleId extends Parsing[RoleId](
    RoleId.withName,
    _.toString,
    (key: String, e: Exception) => s"$key is not a valid role id"
  )

  val roleId: PathBindableExtractor[RoleId] =
    new PathBindableExtractor[RoleId]

}

object PermissionsRouting {

  implicit object bindablePermissionId extends Parsing[PermissionId](
    PermissionId.withName,
    _.toString,
    (key: String, e: Exception) => s"$key is not a valid permission id"
  )

  val permissionId: PathBindableExtractor[PermissionId] =
    new PathBindableExtractor[PermissionId]

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
