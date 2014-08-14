package org.biobank.service

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

}
