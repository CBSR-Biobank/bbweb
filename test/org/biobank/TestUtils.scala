package org.biobank

import org.biobank.domain.{ ConcurrencySafeEntity, DomainValidation }

import com.github.nscala_time.time.Imports._
import org.slf4j.LoggerFactory
import org.scalatest._

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

  implicit class ValidationTests[T](val validation: DomainValidation[T]) {

    /** Executes the function if the validation is successful. If the validation fails then the test fails. To be
      * used in ScalaTest tests.
      *
      *  @param v the validation to test
      *
      *  @param fn the function to execute.
      */
    def mustSucceed(fn: T => Unit) = {
      validation.fold(
        err => fail(err.list.mkString),
        entity => fn(entity)
      )
    }

    /** Looks for an expected message in the validation failure error. If the validation is successful the test
      * fails. To be used in ScalaTest tests.
      *
      *  @param v the validation to test
      *
      *  @param expectedMessage a regular expression to look for in the error message.
      */
    def mustFail(expectedMessages: String*) = {
      validation.fold(
        err => {
          err.list must have length expectedMessages.size
          val errMsgs = err.list.mkString(",")
          expectedMessages.foreach { em =>
            errMsgs must include regex em
          }
        },
        event => fail(s"validation must have failed: $validation")
      )
    }

  }

}
