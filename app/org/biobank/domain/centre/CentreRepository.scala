package org.biobank.domain.centre

import org.biobank.domain._
import org.biobank.domain.centre._

trait CentreRepository extends ReadWriteRepository[CentreId, Centre]

class CentreRepositoryImpl
    extends ReadWriteRepositoryRefImpl[CentreId, Centre](v => v.id)
    with CentreRepository {

  def nextIdentity: CentreId = new CentreId(nextIdentityAsString)

}
