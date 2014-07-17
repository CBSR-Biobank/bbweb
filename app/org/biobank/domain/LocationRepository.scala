package org.biobank.domain

import org.biobank.domain._

import scalaz._
import Scalaz._

trait LocationRepositoryComponent {

  val locationRepository: LocationRepository

  trait LocationRepository extends ReadWriteRepository[LocationId, Location]
}

trait LocationRepositoryComponentImpl extends LocationRepositoryComponent {

  override val locationRepository: LocationRepository = new LocationRepositoryImpl

  class LocationRepositoryImpl
      extends ReadWriteRepositoryRefImpl[LocationId, Location](v => v.id)
      with LocationRepository {

    def nextIdentity: LocationId = new LocationId(nextIdentityAsString)

  }

}
