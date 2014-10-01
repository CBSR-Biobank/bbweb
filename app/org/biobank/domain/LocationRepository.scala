package org.biobank.domain

import org.biobank.domain._

import scalaz._
import Scalaz._

trait LocationRepository extends ReadWriteRepository[LocationId, Location]

class LocationRepositoryImpl
    extends ReadWriteRepositoryRefImpl[LocationId, Location](v => v.id)
    with LocationRepository {

  def nextIdentity: LocationId = new LocationId(nextIdentityAsString)

}
