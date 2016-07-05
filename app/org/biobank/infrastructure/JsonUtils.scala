package org.biobank.infrastructure

import play.api.libs.json._
import org.joda.time.DateTime

object JsonUtils {

  val dateFormat = "yyyy-MM-dd'T'HH:mm:ssZ" // ISO time format

  implicit val jodaDateTimeReads: Reads[DateTime] = Reads.jodaDateReads(dateFormat)
  implicit val jodaDateTimeWrites: Writes[DateTime] = Writes.jodaDateWrites(dateFormat)

}
