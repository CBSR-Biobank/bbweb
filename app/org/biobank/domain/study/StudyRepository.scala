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

  override val NotFoundError = "study with id not found:"

  def nextIdentity: StudyId = new StudyId(nextIdentityAsString)

  def allStudies(): Set[Study] = getValues.toSet

  def getDisabled(id: StudyId): DomainValidation[DisabledStudy] = {
    for {
      study     <- getByKey(id)
      disabaled <- study match {
        case s: DisabledStudy => s.success
        case _ => DomainError(s"study is not disabled: $id").failureNel
      }
    } yield disabaled
  }

  def getEnabled(id: StudyId): DomainValidation[EnabledStudy] = {
    for {
      study   <- getByKey(id)
      enabled <- study match {
        case s: EnabledStudy => s.success
        case _ => DomainError(s"study is not enabled: $id").failureNel
      }
    } yield enabled
  }

  def getRetired(id: StudyId): DomainValidation[RetiredStudy] = {
    for {
      study   <- getByKey(id)
      retired <- study match {
        case s: RetiredStudy => s.success
        case _ => DomainError(s"study is not retired: $id").failureNel
      }
    } yield retired
  }

}
