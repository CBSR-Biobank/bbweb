package org.biobank.domain

import javax.inject.Singleton
import com.google.inject.ImplementedBy

@ImplementedBy(classOf[LocationRepositoryImpl])
trait LocationRepository extends ReadWriteRepository[LocationId, Location]

@Singleton
class LocationRepositoryImpl
    extends ReadWriteRepositoryRefImpl[LocationId, Location](v => v.id)
    with LocationRepository {

  def nextIdentity: LocationId = new LocationId(nextIdentityAsString)

}
