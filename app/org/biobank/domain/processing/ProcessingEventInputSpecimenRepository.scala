package org.biobank.domain.processing

import org.biobank.domain.{
  ReadWriteRepository,
  ReadWriteRepositoryRefImpl
}
import com.google.inject.ImplementedBy
import javax.inject.Singleton
import org.biobank.domain.participants.SpecimenId

/**
 * A record for when a specimen was processed.
 */
@ImplementedBy(classOf[ProcessingEventInputSpecimenRepositoryImpl])
trait ProcessingEventInputSpecimenRepository
    extends ReadWriteRepository[ProcessingEventInputSpecimenId, ProcessingEventInputSpecimen] {

  def withProcessingEventId(processingEventId: ProcessingEventId): Set[ProcessingEventInputSpecimen]

  def withSpecimenId(specimenId: SpecimenId): Set[ProcessingEventInputSpecimen]

}

@Singleton
class ProcessingEventInputSpecimenRepositoryImpl
    extends ReadWriteRepositoryRefImpl[ProcessingEventInputSpecimenId, ProcessingEventInputSpecimen](v => v.id)
    with ProcessingEventInputSpecimenRepository {
  //import org.biobank.CommonValidations._

  def nextIdentity: ProcessingEventInputSpecimenId = new ProcessingEventInputSpecimenId(nextIdentityAsString)

  // private def processingEventIdCriteriaError(processingEventId: ProcessingEventId) =
  //   EntityCriteriaError(
  //     s"no processing event input specimens with processing event ID not found: ${processingEventId.id}")
  //     .toString

  // private def specimenIdCriteriaError(specimenId: SpecimenId) =
  //   EntityCriteriaError(
  //     s"no processing event input specimens with specimen ID not found: ${specimenId.id}").toString

  def withProcessingEventId(processingEventId: ProcessingEventId): Set[ProcessingEventInputSpecimen] =
    getValues.filter(p => p.processingEventId == processingEventId).toSet

  def withSpecimenId(specimenId: SpecimenId): Set[ProcessingEventInputSpecimen] =
    getValues.filter(p => p.specimenId == specimenId).toSet

}
