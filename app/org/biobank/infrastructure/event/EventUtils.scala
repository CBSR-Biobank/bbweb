package org.biobank.infrastructure.event

import org.joda.time._
import org.joda.time.format.ISODateTimeFormat

object EventUtils {

  lazy val ISODateTimeFormatter    = ISODateTimeFormat.dateTime.withZone(DateTimeZone.UTC)
  lazy val ISODateTimeParser       = ISODateTimeFormat.dateTimeParser

}
