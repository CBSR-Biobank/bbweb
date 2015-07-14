package org.biobank.domain.centre

import org.biobank.domain._

import javax.inject.Singleton
import com.google.inject.ImplementedBy
import scalaz.Scalaz._

/** This repository maintains the relationship between a single centre and its multiple locations.
 */
@ImplementedBy(classOf[CentreLocationsRepositoryImpl])
trait CentreLocationsRepository extends ReadWriteRepository[LocationId, CentreLocation] {

  def withCentreId(centreId: CentreId): Set[CentreLocation]

  def withLocationId(locationId: LocationId): DomainValidation[CentreLocation]

}

@Singleton
class CentreLocationsRepositoryImpl
    extends ReadWriteRepositoryRefImpl[LocationId, CentreLocation](v => v.locationId)
    with CentreLocationsRepository {

  override val NotFoundError = "Centre location with id not found:"

  // only existing center and location IDs should be stored, never new IDs
  def nextIdentity = throw new IllegalStateException("should not be used")

  def withCentreId(centreId: CentreId): Set[CentreLocation] = {
    getValues.filter(x => x.centreId == centreId).toSet
  }

  def withLocationId(locationId: LocationId): DomainValidation[CentreLocation] = {
    val centreLocations = getValues.filter(x => x.locationId == locationId).toSet
    if (centreLocations.isEmpty) {
      DomainError(s"location is not for a centre: ${locationId.id}").failureNel
    } else if (centreLocations.size > 1) {
      DomainError(s"location has more than one centre: ${locationId.id}").failureNel
    } else {
      centreLocations.head.success
    }
  }

}
