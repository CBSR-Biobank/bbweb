package org.biobank.controllers

import org.biobank.Global
import org.biobank.domain._
import org.biobank.domain.user._
import org.biobank.fixture.{ ControllerFixture, NameGenerator }
import org.biobank.infrastructure.command.UserCommands._
import org.biobank.domain.JsonHelper._
import org.biobank.service.PasswordHasher

import com.typesafe.plugin._
import org.joda.time.DateTime
import org.scalatest.Tag
import org.scalatestplus.play._
import org.slf4j.LoggerFactory
import play.api.Play.current
import play.api.libs.json._
import play.api.mvc.Cookie
import play.api.test.Helpers._
import play.api.test._
import scaldi.Injectable

/**
  * Tests the REST API for [[User]].
  */
trait PagedResultsSpec {

  def emptyResults(uri: String): Unit = {
    val json = makeRequest(GET, uri)
      (json \ "status").as[String] must include ("success")
      (json \ "data" \ "offset").as[Long] must be (0)
      (json \ "data" \ "total").as[Long] must be (0)
      (json \ "data" \ "prev").as[Option[Int]] must be (None)
      (json \ "data" \ "next").as[Option[Int]] must be (None)

    val jsonList = (json \ "data" \ "items").as[List[JsObject]]
    jsonList must have length 0
  }

  def singleItemResult(uri: String): JsObject = {
    val json = makeRequest(GET, uri)
      (json \ "status").as[String] must include ("success")
      (json \ "data" \ "offset").as[Long] must be (0)
      (json \ "data" \ "total").as[Long] must be (1)
      (json \ "data" \ "prev").as[Option[Int]] must be (None)
      (json \ "data" \ "next").as[Option[Int]] must be (None)

    val jsonList = (json \ "data" \ "items").as[List[JsObject]]
    jsonList must have length 1
    jsonList(0)
  }

  def singleItemResultWithFilter[T <: ConcurrencySafeEntity[_]]
    (filterName: String, filterValue: String, uri: String, resultSize: Int)
      : JsObject = {
    val json = makeRequest(GET, uri + s"?$filterName=$filterValue")
      (json \ "status").as[String] must include ("success")
      (json \ "data" \ "offset").as[Long] must be (0)
      (json \ "data" \ "total").as[Long] must be (1)
      (json \ "data" \ "prev").as[Option[Int]] must be (None)
      (json \ "data" \ "next").as[Option[Int]] must be (None)

    jsonList must have length 1
    jsonList(0)
  }

  def singleItemResultWithStatus[T <: ConcurrencySafeEntity[_]]
    (nameFilter: String, uri: String, resultSize: Int)
      : JsObject = {
    val json = makeRequest(GET, uri + "?filter=" + nameFilter)
      (json \ "status").as[String] must include ("success")
      (json \ "data" \ "offset").as[Long] must be (0)
      (json \ "data" \ "total").as[Long] must be (1)
      (json \ "data" \ "prev").as[Option[Int]] must be (None)
      (json \ "data" \ "next").as[Option[Int]] must be (None)

    jsonList must have length 1
    jsonList(0)
  }
}
