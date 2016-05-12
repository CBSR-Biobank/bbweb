package org.biobank.domain.participants

import org.biobank.domain._

import javax.inject.Singleton
import com.google.inject.ImplementedBy
import scalaz.Scalaz._

@ImplementedBy(classOf[SpecimenRepositoryImpl])
trait SpecimenRepository
    extends ReadWriteRepository [SpecimenId, Specimen] {

  def nextIdentities(count: Int): List[SpecimenId]

  def getForInventoryId(inventoryId: String): DomainValidation[Specimen]

}

@Singleton
class SpecimenRepositoryImpl
    extends ReadWriteRepositoryRefImpl[SpecimenId, Specimen](v => v.id)
    with SpecimenRepository {
  import org.biobank.CommonValidations._

  override val hashidsSalt = "biobank-specimens"

  def nextIdentity: SpecimenId = new SpecimenId(nextIdentityAsString)

  def nextIdentities(count: Int): List[SpecimenId] = {
    val size = getMap.size
    (1 to count).map { index => SpecimenId(hashids.encode(size + index)) }.toList
  }

  def notFound(id: SpecimenId) = IdNotFound(s"specimen id: $id")

  override def getByKey(id: SpecimenId): DomainValidation[Specimen] = {
    getMap.get(id).toSuccessNel(notFound(id).toString)
  }

  def getForInventoryId(inventoryId: String): DomainValidation[Specimen] = {
    getValues.find(s => s.inventoryId == inventoryId)
      .toSuccessNel(EntityCriteriaError(s"specimen with inventory ID not found").toString)
  }

}
