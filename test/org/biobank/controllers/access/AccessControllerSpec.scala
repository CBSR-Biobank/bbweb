package org.biobank.controllers.users

import org.biobank.controllers.PagedResultsSpec
import org.biobank.domain.JsonHelper
import org.biobank.domain.access._
import org.biobank.domain.user._
import org.biobank.fixture.ControllerFixture

/**
 * Tests the REST API for [[User]].
 */
class AccessControllerSpec extends ControllerFixture with JsonHelper with UserFixtures {
  //import org.biobank.TestUtils._

  class activeUserFixture {
    val user = factory.createActiveUser
    userRepository.put(user)
    addMembershipForUser(user)
  }

  private def uri: String = "/access/"

  //private def uri(item: AccessItem): String = uri + s"${item.id.id}"

  private def uri(path: String): String = uri + s"$path"

  private def addMembershipForUser(user: User) = {
    val membership = factory.createMembership.copy(userIds = Set(user.id))
    membershipRepository.put(membership)
  }

  describe("Users REST API") {

    describe("GET /access/roles") {

      it("list the first page of roles") {
        val limit = 5
        val jsonItems = PagedResultsSpec(this).multipleItemsResult(
                uri       = uri("roles") + s"?limit=$limit",
                offset    = 0,
                total     = RoleId.maxId.toLong,
                maybeNext = Some(2),
                maybePrev = None)
        jsonItems must have size limit.toLong
        jsonItems
          .map { json => (json \ "name").as[String] }
          .foreach { jsonName => RoleId.withName(jsonName) }
      }

      it("list a single role filtered by name") {
        val jsonItem = PagedResultsSpec(this)
          .singleItemResult(uri("roles"),
                            Map("filter" -> s"name::${RoleId.ShippingUser.toString}"))

        (jsonItem \ "accessItemType").as[String] must be (AccessItem.roleAccessItemType.toString)

        (jsonItem \ "name").as[String] must be (RoleId.ShippingUser.toString)

        (jsonItem \ "parentIds").as[Set[AccessItemId]].size must be > 0

        (jsonItem \ "childrenIds").as[Set[AccessItemId]].size must be > 0
      }

      it("list roles when sorted by name") {
        val name = "aaaaaaaaaaaa"
        val role = factory.createRole.copy(name = name)
        accessItemRepository.put(role)

        val jsonItems = PagedResultsSpec(this)
          .multipleItemsResult(uri         = uri("roles"),
                               queryParams = Map("sort" -> "name"),
                               offset      = 0,
                               total       = RoleId.maxId.toLong + 1,
                               maybeNext = Some(2),
                               maybePrev = None)
        val firstJsonItem = jsonItems(0)

        (firstJsonItem \ "accessItemType").as[String] must be (AccessItem.roleAccessItemType.toString)

        (firstJsonItem \ "name").as[String] must be (name)

        (firstJsonItem \ "parentIds").as[Set[AccessItemId]].size must be (0)

        (firstJsonItem \ "childrenIds").as[Set[AccessItemId]].size must be (0)
      }

      it("fail when using an invalid query parameters") {
        PagedResultsSpec(this).failWithInvalidParams(uri("roles"))
      }

    }

  }
}
