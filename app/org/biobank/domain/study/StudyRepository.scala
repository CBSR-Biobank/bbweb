package org.biobank.domain.study

import org.biobank.domain._

import javax.inject.Singleton
import com.google.inject.ImplementedBy
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

@ImplementedBy(classOf[StudyRepositoryImpl])
trait StudyRepository extends ReadWriteRepository[StudyId, Study] {

  def allStudies(): Set[Study]

  def getDisabled(id: StudyId): DomainValidation[DisabledStudy]

  def getEnabled(id: StudyId): DomainValidation[EnabledStudy]

  def getRetired(id: StudyId): DomainValidation[RetiredStudy]

}

@Singleton
class StudyRepositoryImpl
    extends ReadWriteRepositoryRefImpl[StudyId, Study](v => v.id)
    with StudyRepository {
  import org.biobank.CommonValidations._

  def nextIdentity: StudyId = new StudyId(nextIdentityAsString)

  def studyNotFound(id: StudyId) = IdNotFound(s"study id: $id")

  override def getByKey(id: StudyId): DomainValidation[Study] = {
    getMap.get(id).toSuccessNel(studyNotFound(id).toString)
  }

  def allStudies(): Set[Study] = getValues.toSet

  def getDisabled(id: StudyId): DomainValidation[DisabledStudy] = {
    for {
      study <- getByKey(id)
      disabled <- {
        study match {
          case s: DisabledStudy => s.successNel[String]
          case s => InvalidStatus(s"study not disabled: $id").failureNel[DisabledStudy]
        }
      }
    } yield disabled
  }

  def getEnabled(id: StudyId): DomainValidation[EnabledStudy] = {
    for {
      study <- getByKey(id)
      enabled <- {
        study match {
          case s: EnabledStudy => s.successNel[String]
          case s => InvalidStatus(s"study not enabled: $id").failureNel[EnabledStudy]
        }
      }
    } yield enabled
  }

  def getRetired(id: StudyId): DomainValidation[RetiredStudy] = {
    for {
      study <- getByKey(id)
      retired <- {
        study match {
          case s: RetiredStudy => s.successNel[String]
          case s => InvalidStatus(s"study not retired: $id").failureNel[RetiredStudy]
        }
      }
    } yield retired
  }

}
