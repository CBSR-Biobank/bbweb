package org.biobank.domain.study

import org.biobank.domain._
import org.slf4j.LoggerFactory

import scalaz._
import Scalaz._

trait SpecimenLinkTypeRepository extends ReadWriteRepository [SpecimenLinkTypeId, SpecimenLinkType] {

  def withId(processingTypeId: ProcessingTypeId, specimenLinkTypeId: SpecimenLinkTypeId): DomainValidation[SpecimenLinkType]

  def allForProcessingType(processingTypeId: ProcessingTypeId): Set[SpecimenLinkType]

  def specimenGroupInUse(specimenGroupId: SpecimenGroupId): Boolean

  def annotationTypeInUse(annotationType: SpecimenLinkAnnotationType): Boolean

}

class SpecimenLinkTypeRepositoryImpl
    extends ReadWriteRepositoryRefImpl[SpecimenLinkTypeId, SpecimenLinkType](v => v.id)
    with SpecimenLinkTypeRepository {

  val log = LoggerFactory.getLogger(this.getClass)

  def nextIdentity: SpecimenLinkTypeId = new SpecimenLinkTypeId(nextIdentityAsString)

  def withId(
    processingTypeId: ProcessingTypeId,
    specimenLinkTypeId: SpecimenLinkTypeId): DomainValidation[SpecimenLinkType] = {
    getByKey(specimenLinkTypeId).fold(
      err =>
      DomainError(
        s"specimen link type does not exist: { processingTypeId: $processingTypeId, specimenLinkTypeId: $specimenLinkTypeId }")
        .failNel,
      slType => if (slType.processingTypeId.equals(processingTypeId))
        slType.success
      else DomainError(
        s"processing type does not have specimen link type:{ processingTypeId: $processingTypeId, specimenLinkTypeId: $specimenLinkTypeId }")
        .failNel
    )
  }

  def allForProcessingType(processingTypeId: ProcessingTypeId): Set[SpecimenLinkType] = {
    getValues.filter(x => x.processingTypeId.equals(processingTypeId)).toSet
  }

  def specimenGroupInUse(specimenGroupId: SpecimenGroupId): Boolean = {
    getValues.exists(slType =>
      (slType.inputGroupId == specimenGroupId) || (slType.outputGroupId == specimenGroupId))
  }

  def annotationTypeInUse(annotationType: SpecimenLinkAnnotationType): Boolean = {
    getValues.exists(slType =>
      slType.annotationTypeData.exists(atd => atd.annotationTypeId.equals(annotationType.id.id)))
  }
}
