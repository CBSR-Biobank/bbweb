package org.biobank.matchers

import play.api.mvc.Result
import play.api.libs.json._
import org.scalatest.matchers.{MatchResult, Matcher}
import scala.concurrent.Future
import scala.util.matching.Regex

trait ApiResultMatchers { this: org.biobank.fixtures.ControllerFixture =>

  import play.api.test.Helpers._

  case class ApiErrorResponse(htmlStatus: Int, messageRegex: String)

  def replyWithError(error: ApiErrorResponse): Matcher[Future[Result]] = new ReplyWithError(error)

  def beOkResponseWithJsonReply(): Matcher[Future[Result]] = new OkRequestWithJsonReplyMatcher

  def beBadRequestWithMessage(errMessage: String): Matcher[Future[Result]] =
    new BadRequestMessageMatcher(errMessage)

 def beForbiddenRequestWithMessage(errMessage: String): Matcher[Future[Result]] =
    new ForbiddenRequestMessageMatcher(errMessage)

  def beNotFoundWithMessage(errMessage: String): Matcher[Future[Result]] =
    new NotFoundMessageMatcher(errMessage)

  def beUnauthorizedNoContent: Matcher[Future[Result]] =
    new ReplyWithNoContent(401)

  private class ReplyWithJsonStatus(htmlStatus: Int, jsonStatus: String) extends Matcher[Future[Result]] {
    override def apply(left: Future[Result]) = {
      val responseStatus = status(left)

      if (responseStatus != htmlStatus) {
        val content = if (contentType(left) == Some("application/json"))
                        Json.prettyPrint(contentAsJson(left))
                      else
                        contentAsString(left)
        MatchResult(false,
                    "got {0} while expecting {1}, response was {2}",
                    "got expected status code {0}",
                    IndexedSeq(responseStatus, htmlStatus, content))
      } else {
        val respContentType = contentType(left)

        if (respContentType != Some("application/json")) {
          MatchResult(false,
                      "bad content type: got: {0}, expected: application/json",
                      "got expected content type {0}",
                      IndexedSeq(respContentType))
        } else {
          val responseJson = contentAsJson(left)
          val status = (responseJson \ "status").as[String]

          log.debug(s"response: status: $responseStatus,\njson: ${Json.prettyPrint(responseJson)}")

          MatchResult(status == jsonStatus,
                      "bad json status: got: {0}, expected: error",
                      "got expected json status {0}",
                      IndexedSeq(status))
        }
      }
    }
  }

  private class ReplyWithNoContent(htmlStatus: Int) extends Matcher[Future[Result]] {
    override def apply(left: Future[Result]) = {
      val responseStatus = status(left)
      log.debug(s"response: status: $responseStatus")

      if (responseStatus != htmlStatus) {
        val content = if (contentType(left) == Some("application/json"))
                        Json.prettyPrint(contentAsJson(left))
                      else
                        contentAsString(left)
           MatchResult(false,
                    "got {0} while expecting {1}, response was {2}",
                    "got expected status code {0}",
                    IndexedSeq(responseStatus, htmlStatus, content))
      } else {
        val responseString = contentAsString(left)
        MatchResult(responseString.isEmpty,
                    "bad response: got: {0}, expected: empty",
                    "got expected reponse",
                    IndexedSeq(responseString))
      }
    }
  }

  private class ReplyWithJson(httpStatus: Int) extends ReplyWithJsonStatus(httpStatus, "success")

  private class ReplyWithError(error: ApiErrorResponse) extends ReplyWithJsonStatus(error.htmlStatus, "error") {
    override def apply(left: Future[Result]) = {
      val superMatchResult = super.apply(left)

      if (!superMatchResult.matches) {
        superMatchResult
      } else {
        val responseJson = contentAsJson(left)
        val message = (responseJson \ "message").as[String]
        val regex = new Regex(error.messageRegex)

        MatchResult(
          regex.findFirstIn(message) != None,
          "message does not contain expected regex: got: {0}, expected: {1}, reponse was {2}",
          "message contains expected regex: got: {0}, expected: {1}, reponse was {2}",
          IndexedSeq(message, error.messageRegex, Json.prettyPrint(responseJson)))
      }
    }
  }

  final private class BadRequestMessageMatcher(errMessage: String)
      extends ReplyWithError(ApiErrorResponse(BAD_REQUEST, errMessage))

  final private class NotFoundMessageMatcher(errMessage: String)
      extends ReplyWithError(ApiErrorResponse(NOT_FOUND, errMessage))

  final private class ForbiddenRequestMessageMatcher(errMessage: String)
      extends ReplyWithError(ApiErrorResponse(FORBIDDEN, errMessage))


  final private class OkRequestWithJsonReplyMatcher extends ReplyWithJson(200)
}
