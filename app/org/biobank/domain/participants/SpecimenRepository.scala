package org.biobank.domain.participants

import org.biobank.domain._

import javax.inject.Singleton
import com.google.inject.ImplementedBy
import scalaz.Scalaz._

@ImplementedBy(classOf[SpecimenRepositoryImpl])
trait SpecimenRepository
    extends ReadWriteRepository [SpecimenId, Specimen] {

  def getByInventoryId(inventoryId: String): DomainValidation[Specimen]

}

@Singleton
class SpecimenRepositoryImpl
    extends ReadWriteRepositoryRefImpl[SpecimenId, Specimen](v => v.id)
    with SpecimenRepository {
  import org.biobank.CommonValidations._

  def nextIdentity: SpecimenId = new SpecimenId(nextIdentityAsString)

  def notFound(id: SpecimenId) = IdNotFound(s"specimen id: $id")

  def inventoryIdCriteriaError(inventoryId: String) =
    EntityCriteriaError(s"specimen with inventory ID not found: $inventoryId")

  override def getByKey(id: SpecimenId): DomainValidation[Specimen] = {
    getMap.get(id).toSuccessNel(notFound(id).toString)
  }

  def getByInventoryId(inventoryId: String): DomainValidation[Specimen] = {
    getValues.find(s => s.inventoryId == inventoryId)
      .toSuccessNel(inventoryIdCriteriaError(inventoryId).toString)
  }

}
