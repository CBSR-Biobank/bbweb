package org.biobank.domain.centre

import org.biobank.domain._
import org.biobank.domain.centre._

import scalaz._
import Scalaz._

/** This repository maintains the relationship between a single centre and its multiple locations.
  */
trait CentreLocationsRepository extends ReadWriteRepository[LocationId, CentreLocation] {

  def withCentreId(centreId: CentreId): Set[CentreLocation]

}

class CentreLocationsRepositoryImpl
    extends ReadWriteRepositoryRefImpl[LocationId, CentreLocation](v => v.locationId)
    with CentreLocationsRepository {

  // only existing center and location IDs should be stored, never new IDs
  def nextIdentity = throw new IllegalStateException("should not be used")

  def withCentreId(centreId: CentreId): Set[CentreLocation] = {
    getValues.filter(x => x.centreId == centreId).toSet
  }

}
