package org.biobank.service.json

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat

object JsonUtils {

  val dateFormat = "yyyy-MM-dd'T'HH:mm:ssZ"

  implicit val jodaDateTimeReads = Reads.jodaDateReads(dateFormat)
  implicit val jodaDateTimeWrites = Writes.jodaDateWrites(dateFormat)

}

