package org.biobank.domain.study

import org.biobank.domain._

import javax.inject.Singleton
import com.google.inject.ImplementedBy
import scalaz._
import scalaz.Scalaz._

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

  override val hashidsSalt = "biobank-studies"

  def nextIdentity: StudyId = new StudyId(nextIdentityAsString)

  def studyNotFound(id: StudyId) = IdNotFound(s"study id: $id")

  override def getByKey(id: StudyId): DomainValidation[Study] = {
    getMap.get(id).toSuccessNel(studyNotFound(id).toString)
  }

  def allStudies(): Set[Study] = getValues.toSet

  def getDisabled(id: StudyId): DomainValidation[DisabledStudy] = {
    getByKey(id) match {
      case Success(s: DisabledStudy) => s.success
      case Success(s) => InvalidStatus(s"study is not disabled: $id").failureNel
      case Failure(err) => err.failure[DisabledStudy]
    }
  }

  def getEnabled(id: StudyId): DomainValidation[EnabledStudy] = {
    getByKey(id) match {
      case Success(s: EnabledStudy) => s.success
      case Success(s) => InvalidStatus(s"study is not enabled: $id").failureNel
      case Failure(err) => err.failure[EnabledStudy]
    }
  }

  def getRetired(id: StudyId): DomainValidation[RetiredStudy] = {
    getByKey(id) match {
      case Success(s: RetiredStudy) => s.success
      case Success(s) => InvalidStatus(s"study is not retired: $id").failureNel
      case Failure(err) => err.failure[RetiredStudy]
    }
  }

}
