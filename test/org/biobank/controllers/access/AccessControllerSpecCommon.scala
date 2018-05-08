package org.biobank.controllers.access

import org.biobank.domain._
import org.biobank.fixtures.ControllerFixture
import play.api.libs.json._
import play.api.test.Helpers._

trait AccessControllerSpecCommon
    extends ControllerFixture
    with JsonHelper {

  import org.biobank.matchers.JsonMatchers._

  protected def uri(paths: String*): String = {
    val basePath = "/api/access"
    if (paths.isEmpty) basePath
    else s"$basePath/" + paths.mkString("/")
  }

  def accessEntityNameSharedBehaviour[D, T <: ConcurrencySafeEntity[_] with HasName with HasSlug](
    createEntity: String => T,
    baseUrl: String
  ) (
    matchItems: (List[D], List[T]) => Unit
  ) (
    implicit reads: Reads[D]
  ) {

      it("list multiple item names in ascending order") {
        val items = List(createEntity("ITEM2"),
                         createEntity("ITEM1")).sortBy(_.name)
        items.foreach(addToRepository)

        val reply = makeAuthRequest(GET, baseUrl + "?filter=name:like:ITEM&order=asc").value
        reply must beOkResponseWithJsonReply

        val replyEntities = (contentAsJson(reply) \ "data").validate[List[D]]
        replyEntities must be (jsSuccess)
        matchItems(replyEntities.get, items)
      }

      it("list single study when using a filter") {
        val items = List(createEntity("ITEM2"),
                         createEntity("ITEM1")).sortBy(_.name)
        items.foreach(addToRepository)

        val reply = makeAuthRequest(GET, baseUrl + "?filter=name::ITEM1").value
        reply must beOkResponseWithJsonReply

        val replyEntities = (contentAsJson(reply) \ "data").validate[List[D]]
        replyEntities must be (jsSuccess)
        matchItems(replyEntities.get, items)
      }

      it("list nothing when using a name filter for name not in system") {
        val reply = makeAuthRequest(GET, baseUrl + "?filter=name::abc123").value
        reply must beOkResponseWithJsonReply
        val replyEntities = (contentAsJson(reply) \ "data").validate[List[D]]
        replyEntities must be (jsSuccess)
        replyEntities.get must have size 0
      }

      it("fail for invalid sort field") {
        val reply = makeAuthRequest(GET, baseUrl + "?sort=xxxx").value
        reply must beBadRequestWithMessage("invalid sort field")
      }

  }
}
