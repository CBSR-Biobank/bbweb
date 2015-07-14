package org.biobank.domain.participants

import org.biobank.domain._

import javax.inject.Singleton
import com.google.inject.ImplementedBy

@ImplementedBy(classOf[SpecimenRepositoryImpl])
trait SpecimenRepository
    extends ReadWriteRepository [SpecimenId, Specimen]

@Singleton
class SpecimenRepositoryImpl
    extends ReadWriteRepositoryRefImpl[SpecimenId, Specimen](v => v.id)
    with SpecimenRepository {

  override val NotFoundError = "speicmen with id not found:"

  def nextIdentity: SpecimenId = new SpecimenId(nextIdentityAsString)

}
