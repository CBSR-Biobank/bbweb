package org.biobank.matchers

import play.api.libs.json._
import org.scalatest.matchers.{BeMatcher, MatchResult, Matcher}

// code borrowed from here:
//
// https://github.com/VlachJosef/notes/blob/a29c9624ae02fe27026762db1ada721d7bfa9ed8/scala/play-adt-to-json/src/test/scala/foldright/JsResultMatchers.scala
//
// https://github.com/lglossman/scala-oauth2-deadbolt-redis/blob/f48f0ba57d18963e4bbed47e967341e9fb71f4d6/test/matchers/CustomMatchers.scala
//
// https://github.com/telefonicaid/fiware-cosmos-platform/blob/72efd024a49fa01f32f42d22e1c4ab8af7a553be/cosmos-api/test/es/tid/cosmos/api/test/matchers/JsonMatchers.scala
//
// https://github.com/lglossman/scala-oauth2-deadbolt-redis/blob/f48f0ba57d18963e4bbed47e967341e9fb71f4d6/test/matchers/CustomMatchers.scala

trait JsonMatchers {
  import org.scalatest.Matchers._

  /**
   * Checks to see if `play.api.libs.json.JsResult` is a specific JsSuccess element.
   */
  def beJsSuccess[E](element: E): Matcher[JsResult[E]] = new BeJsSuccessMatcher[E](element)

  /**
   * Checks to see if `play.api.libs.json.JsResult` is a `JsSuccess`.
   */
  def jsSuccess[E]: BeMatcher[JsResult[E]] = new IsJsSuccessMatcher[E]

  def containKey(expectedKey: String): Matcher[JsValue] = new ContainsKeyMatcher(expectedKey)

  def containPath(expectedPath: JsPath): Matcher[JsValue] = new ContainsPathMatcher(expectedPath)

  def containValue(expectedPath: JsPath, expectedValue: JsValue): Matcher[JsValue] =
    new ContainsValueMatcher(expectedPath, expectedValue)

  def containFieldThatMust[T <: JsValue: Manifest](expectedField: String, elemMatcher: Matcher[T]) =
    new Matcher[JsValue] {
      def apply(js: JsValue) = {
        (js \ expectedField).get match {
          case v: T => elemMatcher(v)
          case _ => MatchResult(false,
                               s"no such field $expectedField in $js",
                               s"field $expectedField found in $js")
        }
      }
    }

  def containFieldWithValue[T <: JsValue: Manifest](expectedField: String, expectedValue: T) =
    containFieldThatMust(expectedField, equal(expectedValue))

  def containField(expectedField: String) = containFieldThatMust(expectedField, not be JsUndefined)

  final private class BeJsSuccessMatcher[E](element: E) extends Matcher[JsResult[E]] {
    def apply(jsResult: JsResult[E]): MatchResult = {
      MatchResult(
        jsResult.fold(_ => false, _ == element),
        s"'$jsResult' did not contain an JsSuccess element matching '$element'.",
        s"'$jsResult' contained an JsSuccess element matching '$element', but should not have.")
    }
  }

  final private class IsJsSuccessMatcher[E] extends BeMatcher[JsResult[E]] {
    def apply(jsResult: JsResult[E]): MatchResult =
      MatchResult(jsResult.isSuccess,
                  s"not a JsSuccess when it must have been: '$jsResult'",
                  s"was a JsSuccess when it must *NOT* have been: '$jsResult'")
  }

  final private class ContainsKeyMatcher(expectedKey: String) extends Matcher[JsValue] {

    def apply(left: JsValue) = {
      MatchResult((left \ expectedKey).toOption.isDefined,
                  s"""JSON $left does not contain key "$expectedKey"""",
                  s"""JSON $left contains key "$expectedKey"""")
    }
  }

  final private class ContainsPathMatcher(expectedPath: JsPath) extends Matcher[JsValue] {
    def apply(left: JsValue) = {
      MatchResult(!(expectedPath(left) isEmpty),
                  s"""JSON $left does not contain path "$expectedPath"""",
                  s"""JSON $left contains path "$expectedPath"""")
    }
  }

  final private class ContainsValueMatcher(expectedPath: JsPath, expectedValue: JsValue)
      extends Matcher[JsValue] {
    def apply(left: JsValue) = {

      val matches = expectedPath(left).foldLeft(false) {
        (matches, value) => {
          matches || value.equals(expectedValue)
        }
      }
      MatchResult(matches,
                  """JSON {0} does not contain value {1} in path "{2}"""",
                  """JSON {0} contains value "[1]" in path "{2}"""",
                  IndexedSeq(Json.prettyPrint(left),
                             Json.prettyPrint(expectedValue),
                             expectedPath))
    }
  }
}

object JsonMatchers extends JsonMatchers
