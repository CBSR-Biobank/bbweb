package org.biobank.service

import org.biobank.domain.DomainValidation
import org.biobank.infrastructure.event.StudyEvents._

import scala.util.Random
import scala.concurrent._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

object Utils {

  def randomString(len: Int): String = {
    val rand = new Random(System.nanoTime)
    val sb = new StringBuilder(len)
    val validChars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
    val validCharsLen = validChars.length
    for (i <- 0 until len) {
      sb.append(validChars(rand.nextInt(validCharsLen)))
    }
    sb.toString
  }

  /**
   * Returns 'true' wrapped in a validation if the event does not fail validation.
   */
  def validationToBoolean(future: Future[DomainValidation[StudyEvent]])
      : Future[DomainValidation[Boolean]] =
    future map { validation => validation map { event => true } }

}
