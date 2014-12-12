package org.biobank

import org.biobank.domain.{ ConcurrencySafeEntity, DomainValidation }

import com.github.nscala_time.time.Imports._
import org.slf4j.LoggerFactory
import org.scalatest._
import org.scalatest.matchers.Matcher
import org.scalatest.matchers.MatchResult

object TestUtils extends MustMatchers with OptionValues {

  val log = LoggerFactory.getLogger(this.getClass)

  val TimeCoparisonMillis = 600L

  def checkTimeStamps[T <: ConcurrencySafeEntity[_]]
    (entity: T, expectedAddedTime: DateTime, expectedLastUpdateTime: Option[DateTime]) = {
    (entity.timeAdded to expectedAddedTime).millis must be < TimeCoparisonMillis
    expectedLastUpdateTime.fold {
      entity.timeModified mustBe (None)
    } {
      dateTime => (entity.timeModified.value to dateTime).millis must be < TimeCoparisonMillis
    }
  }

  def checkTimeStamps[T <: ConcurrencySafeEntity[_]](
    entity: T,
    expectedAddedTime: DateTime,
    expectedLastUpdateTime: DateTime) = {
    //log.info(s"entity: $entity, expectedAddedTime: $expectedAddedTime, expectedLastUpdateTime: $expectedLastUpdateTime")
    (entity.timeAdded to expectedAddedTime).millis must be < TimeCoparisonMillis
    (entity.timeModified.value to expectedLastUpdateTime).millis must be < TimeCoparisonMillis
  }

  case class ListContainsRegexMatcher(regex: String) extends Matcher[List[String]] {
    def apply(list: List[String]): MatchResult = {
      val result = ! list.filter(x => x.matches(regex)).isEmpty
      MatchResult(result,
        s"list did not contain $regex",
        s"list contained $regex but it shouldn't have")
    }
  }

  def containRegex(regex: String) = ListContainsRegexMatcher(regex)

  implicit class ValidationTests[T](val validation: DomainValidation[T]) {

    /** Executes the function if the validation is successful. If the validation fails then the test fails. To be
      * used in ScalaTest tests.
      *
      *  @param fn the function to execute.
      */
    def mustSucceed(fn: T => Unit) = {
      validation.fold(
        err => fail(err.list.mkString(", ")),
        entity => fn(entity)
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
          err.list must have size expectedMessages.size
          expectedMessages.foreach { em =>
            err.list must containRegex (em)
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
          err.list.size must be >= minMessages
          expectedMessages.foreach { em =>
            err.list must containRegex (em)
          }
        },
        event => fail(s"validation must have failed: $validation")
      )
    }

  }

}
