package org.biobank.domain.studies

import com.google.inject.ImplementedBy
import javax.inject.{Inject , Singleton}
import org.biobank.TestData
import org.biobank.domain._
import scalaz.Validation.FlatMap._
import scalaz.Scalaz._

@ImplementedBy(classOf[StudyRepositoryImpl])
trait StudyRepository extends ReadWriteRepositoryWithSlug[StudyId, Study] {

  def allStudies(): Set[Study]

  def getDisabled(id: StudyId): DomainValidation[DisabledStudy]

  def getEnabled(id: StudyId): DomainValidation[EnabledStudy]

  def getRetired(id: StudyId): DomainValidation[RetiredStudy]

}

@Singleton
class StudyRepositoryImpl @Inject() (val testData: TestData)
    extends ReadWriteRepositoryRefImplWithSlug[StudyId, Study](v => v.id)
    with StudyRepository {
  import org.biobank.CommonValidations._

  override def init(): Unit = {
    super.init()
    testData.testStudies.foreach(put)
  }

  def nextIdentity: StudyId = new StudyId(nextIdentityAsString)

  protected def notFound(id: StudyId): IdNotFound = IdNotFound(s"study id: $id")

  protected def slugNotFound(slug: Slug): EntityCriteriaNotFound = EntityCriteriaNotFound(s"study slug: $slug")

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
