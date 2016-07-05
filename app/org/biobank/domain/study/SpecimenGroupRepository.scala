package org.biobank.domain.study

import org.biobank.domain._

import javax.inject.Singleton
import com.google.inject.ImplementedBy
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

@ImplementedBy(classOf[SpecimenGroupRepositoryImpl])
trait SpecimenGroupRepository extends ReadWriteRepository[SpecimenGroupId, SpecimenGroup] {

  def allForStudy(studyId: StudyId): Set[SpecimenGroup]

  def withId(studyId: StudyId, specimenGroupId: SpecimenGroupId)
      : DomainValidation[SpecimenGroup]

}

@Singleton
class SpecimenGroupRepositoryImpl
    extends ReadWriteRepositoryRefImpl[SpecimenGroupId, SpecimenGroup](v => v.id)
    with SpecimenGroupRepository {
  import org.biobank.CommonValidations._

  def nextIdentity: SpecimenGroupId = new SpecimenGroupId(nextIdentityAsString)

  def notFound(id: SpecimenGroupId) = IdNotFound(s"specimen group id: $id")

  override def getByKey(id: SpecimenGroupId): DomainValidation[SpecimenGroup] = {
    getMap.get(id).toSuccessNel(notFound(id).toString)
  }

  def allForStudy(studyId: StudyId): Set[SpecimenGroup] = {
    getValues.filter(x => x.studyId == studyId).toSet
  }

  def withId(studyId: StudyId, specimenGroupId: SpecimenGroupId): DomainValidation[SpecimenGroup] = {
    for {
      sg    <- getByKey(specimenGroupId)
      valid <- {
        if (sg.studyId == studyId) {
          sg.successNel[String]
        } else {
          DomainError(
            s"study does not have specimen group: { studyId: $studyId, specimenGroupId: $specimenGroupId }")
            .failureNel[SpecimenGroup]
        }
      }
    } yield sg
  }
}
