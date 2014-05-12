package org.biobank.domain.study

import org.biobank.domain._

import org.slf4j.LoggerFactory

import scalaz._
import Scalaz._

trait SpecimenLinkTypeRepositoryComponent {

  val specimenLinkTypeRepository: SpecimenLinkTypeRepository

  trait SpecimenLinkTypeRepository extends ReadWriteRepository [SpecimenLinkTypeId, SpecimenLinkType] {

    def nextIdentity: SpecimenLinkTypeId

    def withId(processingTypeId: ProcessingTypeId, specimenLinkTypeId: SpecimenLinkTypeId): DomainValidation[SpecimenLinkType]

    def allForProcessingType(processingTypeId: ProcessingTypeId): Set[SpecimenLinkType]

    def specimenGroupInUse(specimenGroupId: SpecimenGroupId): Boolean

    def annotationTypeInUse(annotationType: SpecimenLinkAnnotationType): Boolean

  }
}

trait SpecimenLinkTypeRepositoryComponentImpl extends SpecimenLinkTypeRepositoryComponent {

  override val specimenLinkTypeRepository: SpecimenLinkTypeRepository = new SpecimenLinkTypeRepositoryImpl

  class SpecimenLinkTypeRepositoryImpl
    extends ReadWriteRepositoryRefImpl[SpecimenLinkTypeId, SpecimenLinkType](v => v.id)
    with SpecimenLinkTypeRepository {

    val log = LoggerFactory.getLogger(this.getClass)

    def nextIdentity: SpecimenLinkTypeId =
      new SpecimenLinkTypeId(java.util.UUID.randomUUID.toString.toUpperCase)

    def withId(
      processingTypeId: ProcessingTypeId,
      specimenLinkTypeId: SpecimenLinkTypeId): DomainValidation[SpecimenLinkType] = {
      getByKey(specimenLinkTypeId).fold(
        err =>
        DomainError(
          s"specimen link type does not exist: { processingTypeId: $processingTypeId, specimenLinkTypeId: $specimenLinkTypeId }")
          .failNel,
        slt => if (slt.processingTypeId.equals(processingTypeId))
          slt.success
        else DomainError(
          s"processing type does not have specimen link type:{ processingTypeId: $processingTypeId, specimenLinkTypeId: $specimenLinkTypeId }")
          .failNel
      )
    }

    def allForProcessingType(processingTypeId: ProcessingTypeId): Set[SpecimenLinkType] = {
      getValues.filter(x => x.processingTypeId.equals(processingTypeId)).toSet
    }

    def specimenGroupInUse(specimenGroupId: SpecimenGroupId): Boolean = {
      getValues.exists(slt =>
        (slt.inputGroupId == specimenGroupId) || (slt.outputGroupId == specimenGroupId))
    }

    def annotationTypeInUse(annotationType: SpecimenLinkAnnotationType): Boolean = {
      getValues.exists(slt =>
        slt.annotationTypeData.exists(atd => atd.annotationTypeId.equals(annotationType.id.id)))
    }
  }
}
