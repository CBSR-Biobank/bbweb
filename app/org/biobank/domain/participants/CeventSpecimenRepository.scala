package org.biobank.domain.participants

import org.biobank.domain.{
  DomainValidation,
  DomainError,
  ReadWriteRepository,
  ReadWriteRepositoryRefImpl
}

import javax.inject.Singleton
import com.google.inject.ImplementedBy
import scalaz.Scalaz._

/**
 *
 */
@ImplementedBy(classOf[CeventSpecimenRepositoryImpl])
trait CeventSpecimenRepository extends ReadWriteRepository[SpecimenId, CeventSpecimen] {

  def withCeventId(ceventId: CollectionEventId): Set[CeventSpecimen]

  def withSpecimenId(specimenId: SpecimenId): DomainValidation[CeventSpecimen]

}

@Singleton
class CeventSpecimenRepositoryImpl
    extends ReadWriteRepositoryRefImpl[SpecimenId, CeventSpecimen](v => v.specimenId)
    with CeventSpecimenRepository {

  override val hashidsSalt = "biobank-collection-event-specimens"

  // only existing center and location IDs should be stored, never new IDs
  def nextIdentity = throw new IllegalStateException("should not be used")

  def withCeventId(ceventId: CollectionEventId): Set[CeventSpecimen] = {
    getValues.filter(x => x.ceventId == ceventId).toSet
  }

  def withSpecimenId(specimenId: SpecimenId): DomainValidation[CeventSpecimen] = {
    val ceventSpecimens = getValues.filter(x => x.specimenId == specimenId).toSet
    if (ceventSpecimens.isEmpty) {
      DomainError(s"location is not for a centre: ${specimenId.id}").failureNel
    } else if (ceventSpecimens.size > 1) {
      DomainError(s"location has more than one centre: ${specimenId.id}").failureNel
    } else {
      ceventSpecimens.head.success
    }
  }

}
