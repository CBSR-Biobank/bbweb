package org.biobank.infrastructure

import play.api.libs.json._

object JsonUtils {

  val dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ" // ISO time format

  implicit val jodaDateTimeReads = Reads.jodaDateReads(dateFormat)
  implicit val jodaDateTimeWrites = Writes.jodaDateWrites(dateFormat)

}

