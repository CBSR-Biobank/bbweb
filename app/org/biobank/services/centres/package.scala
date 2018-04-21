package org.biobank.services

import org.biobank.domain.Location
import org.biobank.domain.centres.Centre
import play.api.libs.json._

package centres {

  final case class CentreCountsByStatus(total: Long, disabledCount: Long, enabledCount: Long)

  object CentreCountsByStatus {

    implicit val centreCountsByStatusFormat: Format[CentreCountsByStatus] = Json.format[CentreCountsByStatus]
  }

  final case class CentreLocation(centreId:     String,
                                  locationId:   String,
                                  centreName:   String,
                                  locationName: String)

  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  object CentreLocation {

    def apply(centre: Centre, location: Location): CentreLocation =
      CentreLocation(centre.id.id, location.id.id, centre.name, location.name)

    implicit val centreLocationFormat: Format[CentreLocation] = Json.format[CentreLocation]

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

    def apply(cl: CentreLocation): CentreLocationInfo =
      CentreLocationInfo(cl.centreId, cl.locationId, cl.centreName, cl.locationName)

    implicit val centreLocationInfoFormat: Format[CentreLocationInfo] = Json.format[CentreLocationInfo]

  }

}
