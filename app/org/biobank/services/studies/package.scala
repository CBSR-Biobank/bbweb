package org.biobank.services

import play.api.libs.json._

package studies {

  final case class StudyCountsByStatus(total:         Long,
                                       disabledCount: Long,
                                       enabledCount:  Long,
                                       retiredCount:  Long)

  object StudyCountsByStatus {

    implicit val studyCountsByStatusFormat: Format[StudyCountsByStatus] = Json.format[StudyCountsByStatus]
  }

}
