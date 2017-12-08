package org.biobank.domain.participants

import com.google.inject.ImplementedBy
import javax.inject.{Inject, Singleton}
import org.biobank.TestData
import org.biobank.domain._
import scalaz.Scalaz._

@ImplementedBy(classOf[SpecimenRepositoryImpl])
trait SpecimenRepository
    extends ReadWriteRepository [SpecimenId, Specimen] {

  def getByInventoryId(inventoryId: String): DomainValidation[Specimen]

}

@Singleton
class SpecimenRepositoryImpl @Inject() (val testData: TestData)
    extends ReadWriteRepositoryRefImpl[SpecimenId, Specimen](v => v.id)
    with SpecimenRepository {
  import org.biobank.CommonValidations._

  override def init(): Unit = {
    super.init()
    testData.testSpecimens.foreach(put)
  }

  def nextIdentity: SpecimenId = new SpecimenId(nextIdentityAsString)

  protected def notFound(id: SpecimenId): IdNotFound = IdNotFound(s"specimen id: $id")

  def inventoryIdCriteriaError(inventoryId: String): String =
    EntityCriteriaError(s"specimen with inventory ID not found: $inventoryId").toString

  def getByInventoryId(inventoryId: String): DomainValidation[Specimen] = {
    getValues.find(s => s.inventoryId == inventoryId)
      .toSuccessNel(inventoryIdCriteriaError(inventoryId))
  }

}
