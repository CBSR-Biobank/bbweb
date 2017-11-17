package org.biobank.service

import play.api.libs.json._

package centres {

  final case class CentreCountsByStatus(total: Long, disabledCount: Long, enabledCount: Long)

  object CentreCountsByStatus {

    implicit val centreCountsByStatusWriter: Writes[CentreCountsByStatus] = Json.writes[CentreCountsByStatus]
  }

  final case class CentreLocation(centreId:     String,
                                  locationId:   String,
                                  centreName:   String,
                                  locationName: String)

  object CentreLocation {

    implicit val centreLocationWriter: Writes[CentreLocation] = Json.writes[CentreLocation]

  }

  final case class CentreLocationInfo(centreId:   String,
                                      locationId: String,
                                      name:       String)

  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  object CentreLocationInfo {

    def apply(centreId: String,
              locationId: String,
              centreName: String,
              locationName: String): CentreLocationInfo =
      CentreLocationInfo(centreId, locationId, s"$centreName: $locationName")

    implicit val centreLocationInfoWriter: Writes[CentreLocationInfo] = Json.writes[CentreLocationInfo]

  }

}
