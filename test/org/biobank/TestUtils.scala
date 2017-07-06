package org.biobank

import java.time.{Duration, OffsetDateTime}
import org.biobank.domain.{ConcurrencySafeEntity, DomainValidation}
import org.scalatest._
import org.scalatest.matchers.MatchResult
import org.scalatest.matchers.Matcher
import org.slf4j.LoggerFactory

object TestUtils extends MustMatchers with OptionValues {

  val log = LoggerFactory.getLogger(this.getClass)

  val TimeCoparisonSeconds = 2L

  def checkOpionalTime(expectedTimeMaybe: Option[OffsetDateTime], actualTimeMaybe: Option[OffsetDateTime]) = {
    expectedTimeMaybe match {
      case Some(expectedTime) => actualTimeMaybe match {
        case Some(actualTime) => checkTimeStamps(expectedTime, actualTime)
        case None => fail(s"actual time is None, but expected had a value")
      }
      case None => actualTimeMaybe match {
        case Some(actualTime) => fail(s"expected time was NONE, but actual time had a value")
        case None => ()
      }
    }
  }

  def checkTimeStamps(expectedTime:  OffsetDateTime,
                      actualTime: OffsetDateTime,
                      maxDifference: Long): Unit = {
    val timediff = Duration.between(actualTime, expectedTime).getSeconds.abs
    timediff must be < maxDifference
    ()
  }

  def checkTimeStamps(expectedTime:  OffsetDateTime, actualTime: OffsetDateTime): Unit = {
    checkTimeStamps(expectedTime, actualTime, TimeCoparisonSeconds)
  }

  def checkTimeStamps(entity:               ConcurrencySafeEntity[_],
                      expectedTimeAdded:    OffsetDateTime,
                      expectedTimeModified: Option[OffsetDateTime]): Unit = {
    checkTimeStamps(entity.timeAdded, expectedTimeAdded)
    checkOpionalTime(expectedTimeModified, entity.timeModified)
  }

  def checkTimeStamps(entity:               ConcurrencySafeEntity[_],
                      expectedTimeAdded:    OffsetDateTime,
                      expectedTimeModified: OffsetDateTime): Unit = {
    checkTimeStamps(entity, expectedTimeAdded, Some(expectedTimeModified))
  }

  /**
   * Searches for an item in list matching regex. The string in str is the string originally searched for.
   *
   * @param list the list of strings to search in.
   *
   * @param regex the regular expression to search for.
   *
   * @param str the original string used to create the regular expression.
   */
  def findMatch(list: List[String], regex: String, str: String) = {
    val result = list.filter(x => regex.r.findAllIn(x).length > 0).nonEmpty
    MatchResult(result,
                s"""list did not contain "$str": $list""",
                s"""list contained "$str" but it shouldn't have: $list""")

  }

  case class ListItemMatchesRegexMatcher(str: String) extends Matcher[List[String]] {
    def apply(list: List[String]): MatchResult = findMatch(list, s"^$str$$", str)
  }

  case class ListItemContainsRegexMatcher(str: String) extends Matcher[List[String]] {
    def apply(list: List[String]): MatchResult = findMatch(list, str, str)
  }

  def containItemMatchingRegex(regex: String) = ListItemMatchesRegexMatcher(regex)

  def containItemContainingRegex(regex: String) = ListItemContainsRegexMatcher(regex)

  implicit class ValidationTests[T](val validation: DomainValidation[T]) {

    /** Executes the function if the validation is successful. If the validation fails then the test fails. To be
      * used in ScalaTest tests.
      *
      *  @param fn the function to execute.
      */
    def mustSucceed(fn: T => Any): Unit = {
      validation.fold(
        err => fail(err.list.toList.mkString(", ")),
        entity => {
          fn(entity)
          ()
        }
      )
    }

    /** Looks for an expected message in the validation failure error. If the validation is successful the test
      * fails. To be used in ScalaTest tests.
      *
      *  @param expectedMessages one or more regular expression to look for in the error list.
      */
    def mustFail(expectedMessages: String*): Unit = {
      validation.fold(
        err => {
          val errList = err.list.toList
          errList must have size expectedMessages.size.toLong
          expectedMessages.foreach { em =>
            errList must containItemMatchingRegex (em)
          }
        },
        event => fail(s"validation must have failed: $validation")
      )
    }

    /** Looks for an expected message in the validation failure error. If the validation is successful the test
      * fails. To be used in ScalaTest tests.
      *
      *  @param minMessages the minimum number of messages expected in the error list.
      *  @param expectedMessages one or more regular expressions to look for in the error message.
      */
    def mustFail(minMessages: Int, expectedMessages: String*): Unit = {
      validation.fold(
        err => {
          err.list.toList.size must be >= minMessages
          expectedMessages.foreach { em =>
            err.list.toList must containItemMatchingRegex (em)
          }
        },
        event => fail(s"validation must have failed: $validation")
      )
    }

    /** Looks for an expected message in the validation failure error. If the validation is successful the test
      * fails. To be used in ScalaTest tests.
      *
      *  @param expectedMessages one or more regular expression to look for in the error list.
      */
    def mustFailContains(expectedMessages: String*): Unit = {
      validation.fold(
        err => {
          err.list.toList must have size expectedMessages.size.toLong
          expectedMessages.foreach { em =>
            err.list.toList must containItemContainingRegex (em)
          }
        },
        event => fail(s"validation must have failed: $validation")
      )
    }

  }

}
