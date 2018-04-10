package org.biobank.service

import com.trueaccord.scalapb.GeneratedMessage
import scala.concurrent._
import scala.util.Random

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
  @SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
  def eventValidationToBoolean(future: Future[ServiceValidation[GeneratedMessage]])
                              (implicit ec: ExecutionContext): Future[ServiceValidation[Boolean]] =
    future map { validation => validation map { event => true } }

}
