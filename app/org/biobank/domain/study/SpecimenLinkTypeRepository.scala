package org.biobank.domain.study

import org.biobank.domain._

import javax.inject.Singleton
import com.google.inject.ImplementedBy
import scalaz._
import Scalaz._

@ImplementedBy(classOf[SpecimenLinkTypeRepositoryImpl])
trait SpecimenLinkTypeRepository extends ReadWriteRepository [SpecimenLinkTypeId, SpecimenLinkType] {

  def withId(processingTypeId: ProcessingTypeId, specimenLinkTypeId: SpecimenLinkTypeId): DomainValidation[SpecimenLinkType]

  def allForProcessingType(processingTypeId: ProcessingTypeId): Set[SpecimenLinkType]

  def specimenSpecCanBeUpdated(specimenGroupId: SpecimenGroupId): Boolean

}

@Singleton
class SpecimenLinkTypeRepositoryImpl
    extends ReadWriteRepositoryRefImpl[SpecimenLinkTypeId, SpecimenLinkType](v => v.id)
    with SpecimenLinkTypeRepository {

  def nextIdentity: SpecimenLinkTypeId = new SpecimenLinkTypeId(nextIdentityAsString)

  def withId(
    processingTypeId: ProcessingTypeId,
    specimenLinkTypeId: SpecimenLinkTypeId): DomainValidation[SpecimenLinkType] = {
    getByKey(specimenLinkTypeId).fold(
      err =>
      DomainError(
        s"specimen link type does not exist: { processingTypeId: $processingTypeId, specimenLinkTypeId: $specimenLinkTypeId }")
        .failureNel,
      slType =>
      if (slType.processingTypeId == processingTypeId)
        slType.success
      else DomainError(
        s"processing type does not have specimen link type:{ processingTypeId: $processingTypeId, specimenLinkTypeId: $specimenLinkTypeId }")
        .failureNel
    )
  }

  def allForProcessingType(processingTypeId: ProcessingTypeId): Set[SpecimenLinkType] = {
    getValues.filter(x => x.processingTypeId == processingTypeId).toSet
  }

  def specimenSpecCanBeUpdated(specimenGroupId: SpecimenGroupId): Boolean = {
    getValues.exists(slType =>
      (slType.inputGroupId == specimenGroupId) || (slType.outputGroupId == specimenGroupId))
  }

}
