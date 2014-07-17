package org.biobank.domain.centre

import org.biobank.domain._
import org.biobank.domain.centre._

import scalaz._
import Scalaz._

trait CentreRepositoryComponent {

  val centreRepository: CentreRepository

  trait CentreRepository extends ReadWriteRepository[CentreId, Centre]
}

trait CentreRepositoryComponentImpl extends CentreRepositoryComponent {

  override val centreRepository: CentreRepository = new CentreRepositoryImpl

  class CentreRepositoryImpl extends ReadWriteRepositoryRefImpl[CentreId, Centre](v => v.id) with CentreRepository {

    def nextIdentity: CentreId = new CentreId(nextIdentityAsString)

  }

}
