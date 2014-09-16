package org.biobank

import org.biobank.domain.ConcurrencySafeEntity

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

}
