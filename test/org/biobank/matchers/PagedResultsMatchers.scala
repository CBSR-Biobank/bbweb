package org.biobank.matchers

import org.biobank.{SystemError, SystemValidation}
import org.biobank.fixtures.Url
import org.scalatest.matchers._
import play.api.libs.json._
import play.api.test.Helpers._
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

trait PagedResultsMatchers extends ApiResultMatchers { this: org.biobank.fixtures.ControllerFixture =>

  def beEmptyResults: Matcher[Url] = new EmptyResultsMatcher

  def beSingleItemResults(offset:    Long = 0,
                          maybeNext: Option[Int] = None,
                          maybePrev: Option[Int] = None): Matcher[JsValue] =
    new SingleItemResultsMatcher(offset, maybeNext, maybePrev)

  def beMultipleItemResults(offset:    Long = 0,
                            total:     Long,
                            maybeNext: Option[Int] = None,
                            maybePrev: Option[Int] = None): Matcher[JsValue] =
    new MultipleItemsResultsMatcher(offset, total, maybeNext, maybePrev)

  private final class EmptyResultsMatcher extends Matcher[Url] {

    def apply(left: Url): MatchResult = {
      makePagedRequest(left).fold (
        err => MatchResult(false, err.toList.mkString(","), ""),
        json => {
          val jsonTotal = (json \ "data" \ "total").as[Long]
          val jsonItems = (json \ "data" \ "items").as[List[JsObject]]

          if (jsonTotal != 0) {
            MatchResult(false, s"total is not 0", "")
          } else if (jsonItems.length > 0) {
            MatchResult(false, "items is not empty", "")
          } else {
            new PagedResultsMatcher(0, None, None).apply(json)
          }

        }
      )
    }
  }

  private final class SingleItemResultsMatcher(offset:    Long,
                                               maybeNext: Option[Int],
                                               maybePrev: Option[Int])
      extends PagedResultsMatcher(offset, maybeNext, maybePrev) {

    override def apply(left: JsValue): MatchResult = {
      val jsonTotal = (left \ "data" \ "total").as[Long]
      val jsonItems = (left \ "data" \ "items").as[List[JsObject]]

          if (jsonTotal < 1) {
            MatchResult(false, s"total is zero", "")
          } else if (jsonItems.length != 1) {
            MatchResult(false, "items length is not 1", "")
          } else {
        super.apply(left)
      }
    }
  }

  private final class MultipleItemsResultsMatcher(offset:    Long,
                                                  total:     Long,
                                                  maybeNext: Option[Int],
                                                  maybePrev: Option[Int])
      extends PagedResultsMatcher(offset, maybeNext, maybePrev) {

    override def apply(left: JsValue): MatchResult = {
      val jsonTotal = (left \ "data" \ "total").as[Long]
      val jsonItems = (left \ "data" \ "items").as[List[JsObject]]

          if (jsonTotal != total) {
            MatchResult(false, s"total is not $total", "")
          } else if (jsonItems.length < 1) {
            MatchResult(false, "items length is not greater than zero", "")
          } else {
        super.apply(left)
          }
        }
  }

  class PagedResultsMatcher(offset:    Long,
                                    maybeNext: Option[Int],
                                    maybePrev: Option[Int]) extends Matcher[JsValue] {

    override def apply(left: JsValue): MatchResult = {
      val jsonStatus = (left \ "status").as[String]
      val jsonOffset = (left \ "data" \ "offset").as[Long]
      val jsonNext = (left \ "data" \ "next").asOpt[Int]
      val jsonPrev = (left \ "data" \ "prev").asOpt[Int]

      if (jsonStatus != "success") {
        MatchResult(false, "reply was not successful", "")
      } else if (jsonOffset != offset) {
        MatchResult(false, s"offset is not $offset", "")
      } else if (jsonNext != maybeNext) {
        MatchResult(false, s"next is not $maybeNext", "")
      } else if (jsonPrev != maybePrev) {
        MatchResult(false, s"prev is not $maybePrev", "")
      } else {
        MatchResult(true, "", "")
      }
    }
  }

  private def makePagedRequest(url: Url): SystemValidation[JsValue] = {
    for {
      reply <- makeAuthRequest(GET, url.path).toSuccessNel("Request is invalid")
      okResponse <- {
        val replyCheck = beOkResponseWithJsonReply.apply(reply)
        if (!replyCheck.matches) {
          SystemError(replyCheck.failureMessage).failureNel[MatchResult]
        } else {
          replyCheck.successNel[String]
        }
      }
    } yield contentAsJson(reply)
  }

}
