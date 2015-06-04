package org.biobank.domain.centre

import org.biobank.domain._

import javax.inject.Singleton
import com.google.inject.ImplementedBy

@ImplementedBy(classOf[CentreRepositoryImpl])
trait CentreRepository extends ReadWriteRepository[CentreId, Centre]

@Singleton
class CentreRepositoryImpl
    extends ReadWriteRepositoryRefImpl[CentreId, Centre](v => v.id)
    with CentreRepository {

  def nextIdentity: CentreId = new CentreId(nextIdentityAsString)

}
