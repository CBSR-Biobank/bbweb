package org.biobank.domain.participants

import org.biobank.domain._

import javax.inject.Singleton
import com.google.inject.ImplementedBy
import scalaz.Scalaz._

@ImplementedBy(classOf[SpecimenRepositoryImpl])
trait SpecimenRepository
    extends ReadWriteRepository [SpecimenId, Specimen] {

  def getForSpecimenSpec(specimenSpecId: String): Set[Specimen]

}

@Singleton
class SpecimenRepositoryImpl
    extends ReadWriteRepositoryRefImpl[SpecimenId, Specimen](v => v.id)
    with SpecimenRepository {
  import org.biobank.CommonValidations._

  override val hashidsSalt = "biobank-specimens"

  def nextIdentity: SpecimenId = new SpecimenId(nextIdentityAsString)

  def notFound(id: SpecimenId) = IdNotFound(s"specimen id: $id")

  override def getByKey(id: SpecimenId): DomainValidation[Specimen] = {
    getMap.get(id).toSuccessNel(notFound(id).toString)
  }

  def getForSpecimenSpec(specimenSpecId: String): Set[Specimen] = {
    ???
  }

}
