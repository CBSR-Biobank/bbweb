package org.biobank.domain.study

import org.biobank.domain._
import org.slf4j.LoggerFactory

import javax.inject.Singleton
import com.google.inject.ImplementedBy
import scalaz._
import Scalaz._

@ImplementedBy(classOf[SpecimenLinkTypeRepositoryImpl])
trait SpecimenLinkTypeRepository extends ReadWriteRepository [SpecimenLinkTypeId, SpecimenLinkType] {

  def withId(processingTypeId: ProcessingTypeId, specimenLinkTypeId: SpecimenLinkTypeId): DomainValidation[SpecimenLinkType]

  def allForProcessingType(processingTypeId: ProcessingTypeId): Set[SpecimenLinkType]

  def specimenGroupCanBeUpdated(specimenGroupId: SpecimenGroupId): Boolean

  def annotationTypeInUse(annotationType: SpecimenLinkAnnotationType): Boolean

}

@Singleton
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

  def specimenGroupCanBeUpdated(specimenGroupId: SpecimenGroupId): Boolean = {
    getValues.exists(slType =>
      (slType.inputGroupId == specimenGroupId) || (slType.outputGroupId == specimenGroupId))
  }

  def annotationTypeInUse(annotationType: SpecimenLinkAnnotationType): Boolean = {
    getValues.exists(slType =>
      slType.annotationTypeData.exists(atd => atd.annotationTypeId == annotationType.id.id))
  }
}
