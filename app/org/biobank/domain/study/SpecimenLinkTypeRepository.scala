package org.biobank.domain.study

import org.biobank.domain._

import javax.inject.Singleton
import com.google.inject.ImplementedBy
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

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
  import org.biobank.CommonValidations._

  def nextIdentity: SpecimenLinkTypeId = new SpecimenLinkTypeId(nextIdentityAsString)

  def notFound(id: SpecimenLinkTypeId) = IdNotFound(s"specimen link type id: $id")

  override def getByKey(id: SpecimenLinkTypeId): DomainValidation[SpecimenLinkType] = {
    getMap.get(id).toSuccessNel(notFound(id).toString)
  }

  def withId(
    processingTypeId: ProcessingTypeId,
    specimenLinkTypeId: SpecimenLinkTypeId): DomainValidation[SpecimenLinkType] = {
    for {
      slType <- getByKey(specimenLinkTypeId)
      valid  <- {
        if (slType.processingTypeId == processingTypeId)
          slType.successNel[String]
        else DomainError(
          s"processing type does not have specimen link type:{ processingTypeId: $processingTypeId, specimenLinkTypeId: $specimenLinkTypeId }"
        ).failureNel[SpecimenLinkType]
      }
    } yield slType
  }

  def allForProcessingType(processingTypeId: ProcessingTypeId): Set[SpecimenLinkType] = {
    getValues.filter(x => x.processingTypeId == processingTypeId).toSet
  }

  def specimenSpecCanBeUpdated(specimenGroupId: SpecimenGroupId): Boolean = {
    getValues.exists(slType =>
      (slType.inputGroupId == specimenGroupId) || (slType.outputGroupId == specimenGroupId))
  }

}
