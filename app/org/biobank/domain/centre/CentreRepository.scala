package org.biobank.domain.centre

import com.google.inject.ImplementedBy
import javax.inject.Singleton
import org.biobank.domain._
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

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
  import org.biobank.CommonValidations._

  def nextIdentity: CentreId = new CentreId(nextIdentityAsString)

  def notFound(id: CentreId) = IdNotFound(s"centre id: $id")

  override def getByKey(id: CentreId): DomainValidation[Centre] = {
    getMap.get(id).toSuccessNel(notFound(id).toString)
  }

  def getDisabled(id: CentreId): DomainValidation[DisabledCentre] = {
    for {
      centre <- getByKey(id)
      disabled <- {
        centre match {
          case c: DisabledCentre => c.successNel[String]
          case c => InvalidStatus(s"centre is not disabled: $id").failureNel[DisabledCentre]
        }
      }
    } yield disabled
  }

  def getEnabled(id: CentreId): DomainValidation[EnabledCentre] = {
    for {
      centre <- getByKey(id)
      enabled <- {
        centre match {
          case c: EnabledCentre => c.successNel[String]
          case c => InvalidStatus(s"centre is not enabled: $id").failureNel[EnabledCentre]
        }
      }
    } yield enabled
  }

  def getByLocationId(uniqueId: String): DomainValidation[Centre] = {
    val centres = getValues.filter { c => !c.locations.filter( l => l.uniqueId == uniqueId ).isEmpty}
    if (centres.isEmpty) {
      EntityCriteriaError(s"centre with location id does not exist: $uniqueId").failureNel[Centre]
    } else if (centres.size > 1){
      EntityCriteriaError(s"multiple centres with location id: $uniqueId").failureNel[Centre]
    } else {
      centres.head.successNel[String]
    }
  }
}
