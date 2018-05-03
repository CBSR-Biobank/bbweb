package org.biobank.controllers

import org.biobank.fixtures._

import play.api.libs.json._
import play.api.test.Helpers._
import org.scalatest._

/**
 * Common code for REST APIs that uses paged results.
 */
case class PagedResultsSpec(fakeApp: BbwebFakeApplication) extends MustMatchers {

  val nameGenerator = new NameGenerator(this.getClass)

  def emptyResults(uri: String): Unit = {
    val json = fakeApp.makeRequest(GET, uri)
    (json \ "status").as[String] must include ("success")
    (json \ "data" \ "offset").as[Long] must be (0)
    (json \ "data" \ "total").as[Long] must be (0)
    (json \ "data" \ "next").asOpt[Int] must be (None)
    (json \ "data" \ "prev").asOpt[Int] must be (None)

    val jsonList = (json \ "data" \ "items").as[List[JsObject]]
    jsonList must have length 0
    ()
  }

  def singleItemResult(uri:         String,
                       queryParams: Map[String, String] =  Map.empty,
                       total:       Long = 1,
                       offset:      Long = 0,
                       maybeNext:   Option[Int] = None,
                       maybePrev:   Option[Int] = None)
      : JsObject = {
    val json = fakeApp.makeRequest(GET, uriWithParams(uri, queryParams))
    (json \ "status").as[String] must include ("success")
    (json \ "data" \ "offset").as[Long] must be (offset)
    (json \ "data" \ "total").as[Long] must be (total)
    (json \ "data" \ "next").asOpt[Int] must be (maybeNext)
    (json \ "data" \ "prev").asOpt[Int] must be (maybePrev)

    val jsonList = (json \ "data" \ "items").as[List[JsObject]]
    jsonList must have length 1
    jsonList(0)
  }

  def multipleItemsResult(uri:         String,
                          queryParams: Map[String, String] =  Map.empty,
                          offset:      Long,
                          total:       Long,
                          maybeNext:   Option[Int],
                          maybePrev:   Option[Int])
      : List[JsObject] = {
    val json = fakeApp.makeRequest(GET, uriWithParams(uri, queryParams))
    (json \ "status").as[String] must include ("success")
    (json \ "data" \ "offset").as[Long] must be (offset)
    (json \ "data" \ "total").as[Long] must be (total)
    (json \ "data" \ "next").asOpt[Int] must be (maybeNext)
    (json \ "data" \ "prev").asOpt[Int] must be (maybePrev)

    (json \ "data" \ "items").as[List[JsObject]]
  }

  def failWithNegativePageNumber(uri: String) = {
    val json = fakeApp.makeRequest(GET, uri + "?page=-1&limit=1", BAD_REQUEST)
    (json \ "status").as[String] must include ("error")
    (json \ "message").as[String] must include ("page is invalid")
  }

  def failWithInvalidPageNumber(uri: String) = {
    // assumes the result will be empty
    val json = fakeApp.makeRequest(GET, uri + "?page=9999", BAD_REQUEST)
    (json \ "status").as[String] must include ("error")
    (json \ "message").as[String] must include ("page exceeds limit")
  }

  def failWithNegativePageSize(uri: String) = {
    val json = fakeApp.makeRequest(GET, uri + "?limit=-1", BAD_REQUEST)
    (json \ "status").as[String] must include ("error")
    (json \ "message").as[String] must include ("page size is invalid")
  }

  def failWithInvalidPageSize(uri: String, limit: Int) = {
    val json = fakeApp.makeRequest(GET, uri + "?limit=" + limit, BAD_REQUEST)
    (json \ "status").as[String] must include ("error")
    (json \ "message").as[String] must include ("page size exceeds maximum")
  }

  def failWithInvalidSort(uri: String) = {
    val json = fakeApp.makeRequest(GET, uri + "?sort=xyz", BAD_REQUEST)
    (json \ "status").as[String] must include ("error")
    (json \ "message").as[String] must include ("invalid sort field")
  }

  def failWithInvalidParams(uri: String, invalidPageSize: Int = 100) =  {
    failWithNegativePageNumber(uri)
    failWithInvalidPageNumber(uri)
    failWithNegativePageSize(uri)
    failWithInvalidPageSize(uri, invalidPageSize);
    failWithInvalidSort(uri)
  }

  private def uriWithParams(baseUri: String, queryParams: Map[String, String]) = {
    if (queryParams.nonEmpty) {
      baseUri + "?" + queryParams.map { case (k,v) => s"$k=$v" }.mkString("&")
    } else {
      baseUri
    }
  }
}
