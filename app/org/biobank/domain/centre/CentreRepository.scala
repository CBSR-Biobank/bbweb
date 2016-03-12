package org.biobank.domain.centre

import org.biobank.domain._

import javax.inject.Singleton
import com.google.inject.ImplementedBy
import scalaz.Scalaz._

@ImplementedBy(classOf[CentreRepositoryImpl])
trait CentreRepository extends ReadWriteRepository[CentreId, Centre] {

  def getDisabled(id: CentreId): DomainValidation[DisabledCentre]

  def getEnabled(id: CentreId): DomainValidation[EnabledCentre]

  def getByLocationId(uniqueId: String): DomainValidation[Centre]

}

@Singleton
class CentreRepositoryImpl
    extends ReadWriteRepositoryRefImpl[CentreId, Centre](v => v.id)
    with CentreRepository {

  def nextIdentity: CentreId = new CentreId(nextIdentityAsString)

  def getDisabled(id: CentreId): DomainValidation[DisabledCentre] = {
    getByKey(id).fold(
      err => DomainError(s"centre with id does not exist: $id").failureNel,
      centre => centre match {
        case centre: DisabledCentre => centre.success
        case centre => DomainError(s"centre is not disabled: $centre").failureNel
      }
    )
  }

  def getEnabled(id: CentreId): DomainValidation[EnabledCentre] = {
    getByKey(id).fold(
      err => DomainError(s"centre with id does not exist: $id").failureNel,
      centre => centre match {
        case centre: EnabledCentre => centre.success
        case centre => DomainError(s"centre is not enabled: $centre").failureNel
      }
    )
  }

  def getByLocationId(uniqueId: String): DomainValidation[Centre] = {
    val centres = getValues.filter { c => !c.locations.filter( l => l.uniqueId == uniqueId ).isEmpty}
    if (centres.isEmpty) {
      DomainError(s"centre with location id does not exist: $uniqueId").failureNel
    } else if (centres.size > 1){
      DomainError(s"multiple centres with location id: $uniqueId").failureNel
    } else {
      centres.head.success
    }
  }
}
