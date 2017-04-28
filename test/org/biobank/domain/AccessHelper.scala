package org.biobank.domain

import javax.inject.{Inject, Singleton}
import org.biobank.Global
import org.biobank.domain.access._
import org.biobank.domain.access.RoleId._
import org.biobank.domain.user.UserId

/**
 * Used for testing Access Control for the different services in the system
 */
@Singleton
class AccessHelper @Inject() (val accessItemRepository: AccessItemRepository) {

  import org.biobank.domain.access.AccessItem._

  def addUserTotUserAdmin(userId: UserId): Unit = {
    val userAdmin = getUserAdmin
    accessItemRepository.put(userAdmin.copy(userIds = userAdmin.userIds + userId))
  }

  def addUserTotWebsiteAdmin(userId: UserId): Unit = {
    val websiteAdmin = getWebsiteAdmin
    accessItemRepository.put(websiteAdmin.copy(userIds = websiteAdmin.userIds + userId))
  }

  private def getUserAdmin(): Role = {
    accessItemRepository.getRole(RoleId.UserAdministrator).valueOr { _ =>
      val r = createRole(roleId      = RoleId.UserAdministrator,
                         description = "UserAdministrator",
                         userIds     = Set.empty[UserId],
                         parentIds   = Set.empty[AccessItemId],
                         childrenIds = Set(PermissionId.UserUpdate,
                                           PermissionId.UserChangeState,
                                           PermissionId.UserRead))
      accessItemRepository.put(r)
      r
    }
  }

  private def getWebsiteAdmin(): Role = {
    accessItemRepository.getRole(RoleId.WebsiteAdministrator).valueOr { _ =>
      val r = createRole(roleId      = RoleId.WebsiteAdministrator,
                         description = "WebsiteAdministrator",
                         userIds     = Set(Global.DefaultUserId),
                         parentIds   = Set.empty[AccessItemId],
                         childrenIds = Set(RoleId.UserAdministrator,
                                           RoleId.StudyAdministrator,
                                           RoleId.CentreAdministrator,
                                           RoleId.ShippingAdministrator,
                                           RoleId.SpecimenCollector,
                                           RoleId.SpecimenProcessor))

      accessItemRepository.put(r)
      r
    }
  }

  @SuppressWarnings(Array("org.wartremover.warts.DefaultArguments"))
  private def createRole(roleId:       RoleId,
                         description:  String,
                         userIds:      Set[UserId],
                         parentIds:    Set[AccessItemId],
                         childrenIds:  Set[AccessItemId]): Role =
    Role(id           = AccessItemId(roleId.toString),
         version      = 0,
         timeAdded    = Global.StartOfTime,
         timeModified = None,
         name         = roleId.toString,
         description  = Some(description),
         ruleName     = "",
         userIds      = userIds,
         parentIds    = parentIds,
         childrenIds  = childrenIds)
}
