package org.biobank.controllers.access

import org.biobank.domain._
import org.biobank.fixture.ControllerFixture
import play.api.libs.json._
import play.api.test.Helpers._

trait AccessControllerSpecCommon
    extends ControllerFixture
    with JsonHelper {

  protected def uri(paths: String*): String = {
    val basePath = "/api/access"
    if (paths.isEmpty) basePath
    else s"$basePath/" + paths.mkString("/")
  }

  def accessEntityNameSharedBehaviour[T <: ConcurrencySafeEntity[_] with HasName with HasSlug]
    (createEntity: String => T, baseUrl: String) {

      it("list multiple item names in ascending order") {
        val items = List(createEntity("ITEM2"),
                         createEntity("ITEM1"))
        items.foreach(addToRepository)

        val json = makeRequest(GET, baseUrl + "?filter=name:like:ITEM&order=asc")

        (json \ "status").as[String] must include ("success")

        val jsonList = (json \ "data").as[List[JsObject]]
        jsonList must have size items.size.toLong

        compareNameDto(jsonList(0), items(1))
        compareNameDto(jsonList(1), items(0))
      }

      it("list single study when using a filter") {
        val items = List(createEntity("ITEM2"),
                         createEntity("ITEM1"))
        items.foreach(addToRepository)

        val json = makeRequest(GET, baseUrl + "?filter=name::ITEM1")

        (json \ "status").as[String] must include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]
        jsonList must have size 1

        compareNameDto(jsonList(0), items(1))
      }

      it("list nothing when using a name filter for name not in system") {
        val json = makeRequest(GET, baseUrl + "?filter=name::abc123")

        (json \ "status").as[String] must include ("success")

        val jsonList = (json \ "data").as[List[JsObject]]
        jsonList must have size 0
      }

      it("fail for invalid sort field") {
        val json = makeRequest(GET, baseUrl + "?sort=xxxx", BAD_REQUEST)

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include ("invalid sort field")
      }

  }
}
