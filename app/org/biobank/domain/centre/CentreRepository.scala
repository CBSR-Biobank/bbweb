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

  override val NotFoundError = "centre with id not found:"

  def nextIdentity: CentreId = new CentreId(nextIdentityAsString)

}
