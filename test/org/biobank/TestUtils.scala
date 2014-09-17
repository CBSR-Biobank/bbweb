package org.biobank

import org.biobank.domain.{ ConcurrencySafeEntity, DomainValidation }

import org.scalatest._
import Matchers._
import org.scalatest.OptionValues._
import com.github.nscala_time.time.Imports._
import org.slf4j.LoggerFactory

object TestUtils {

  val log = LoggerFactory.getLogger(this.getClass)

  val TimeCoparisonMillis = 600L

  def checkTimeStamps[T <: ConcurrencySafeEntity[_]](
    entity: T,
    expectedAddedTime: DateTime,
    expectedLastUpdateTime: Option[DateTime]) = {
    (entity.addedDate to expectedAddedTime).millis should be < TimeCoparisonMillis
    expectedLastUpdateTime.fold {
      entity.lastUpdateDate should be (None)
    } {
      dateTime => (entity.lastUpdateDate.value to dateTime).millis should be < TimeCoparisonMillis
    }
  }

  def checkTimeStamps[T <: ConcurrencySafeEntity[_]](
    entity: T,
    expectedAddedTime: DateTime,
    expectedLastUpdateTime: DateTime) = {
    //log.info(s"entity: $entity, expectedAddedTime: $expectedAddedTime, expectedLastUpdateTime: $expectedLastUpdateTime")
    (entity.addedDate to expectedAddedTime).millis should be < TimeCoparisonMillis
    (entity.lastUpdateDate.value to expectedLastUpdateTime).millis should be < TimeCoparisonMillis
  }

  implicit class ValidationTests[T](val validation: DomainValidation[T]) {

    /** Executes the function if the validation is successful. If the validation fails then the test fails. To be
      * used in ScalaTest tests.
      *
      *  @param v the validation to test
      *
      *  @param fn the function to execute.
      */
    def shouldSucceed(fn: T => Unit) = {
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
    def shouldFail(expectedMessage: String) = {
      validation.fold(
        err => {
          err.list should have length 1
          err.list.head should include regex expectedMessage
        },
        event => fail(s"validation should have failed: $validation")
      )
    }

  }

}
