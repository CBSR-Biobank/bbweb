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

  // only existing center and location IDs should be stored, never new IDs
  @SuppressWarnings(Array("org.wartremover.warts.Throw"))
  def nextIdentity: SpecimenId = throw new IllegalStateException("should not be used")

  def withCeventId(ceventId: CollectionEventId): Set[CeventSpecimen] = {
    getValues.filter(x => x.ceventId == ceventId).toSet
  }

  def withSpecimenId(specimenId: SpecimenId): DomainValidation[CeventSpecimen] = {
    val ceventSpecimens = getValues.filter(x => x.specimenId == specimenId).toSet
    if (ceventSpecimens.isEmpty) {
      DomainError(s"cevent specimen repository: specimen id not found: ${specimenId.id}")
        .failureNel[CeventSpecimen]
    } else if (ceventSpecimens.size > 1) {
      DomainError(s"cevent specimen repository: more than one entry found for scpecimen: ${specimenId.id}")
        .failureNel[CeventSpecimen]
    } else {
      ceventSpecimens.head.successNel[String]
    }
  }

}
