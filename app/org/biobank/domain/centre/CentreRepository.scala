package org.biobank.domain.centre

import com.google.inject.ImplementedBy
import javax.inject.{Inject , Singleton}
import org.biobank.TestData
import org.biobank.domain._
import org.biobank.domain.study.StudyId
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

@ImplementedBy(classOf[CentreRepositoryImpl])
trait CentreRepository extends ReadWriteRepository[CentreId, Centre] {

  def getDisabled(id: CentreId): DomainValidation[DisabledCentre]

  def getEnabled(id: CentreId): DomainValidation[EnabledCentre]

  def getByLocationId(id: LocationId): DomainValidation[Centre]

  def withStudy(studyId: StudyId): Set[Centre]

  def getByNames(names: Set[String]): Set[Centre]

}

@Singleton
class CentreRepositoryImpl @Inject() (val testData: TestData)
    extends ReadWriteRepositoryRefImpl[CentreId, Centre](v => v.id)
    with CentreRepository {
  import org.biobank.CommonValidations._

  def nextIdentity: CentreId = new CentreId(nextIdentityAsString)

  def notFound(id: CentreId): IdNotFound = IdNotFound(s"centre id: $id")

  override def getByKey(id: CentreId): DomainValidation[Centre] = {
    getMap.get(id).toSuccessNel(notFound(id).toString)
  }

  def getDisabled(id: CentreId): DomainValidation[DisabledCentre] = {
    for {
      centre <- getByKey(id)
      disabled <- {
        centre match {
          case c: DisabledCentre => c.successNel[String]
          case c => InvalidStatus(s"centre is not disabled: $id").failureNel[DisabledCentre]
        }
      }
    } yield disabled
  }

  def getEnabled(id: CentreId): DomainValidation[EnabledCentre] = {
    for {
      centre <- getByKey(id)
      enabled <- {
        centre match {
          case c: EnabledCentre => c.successNel[String]
          case c => InvalidStatus(s"centre is not enabled: $id").failureNel[EnabledCentre]
        }
      }
    } yield enabled
  }

  def getByLocationId(id: LocationId): DomainValidation[Centre] = {
    val centres = getValues.filter { c => !c.locations.filter( l => l.id == id ).isEmpty}
    if (centres.isEmpty) {
      EntityCriteriaError(s"centre with location id does not exist: $id").failureNel[Centre]
    } else if (centres.size > 1){
      EntityCriteriaError(s"multiple centres with location id: $id").failureNel[Centre]
    } else {
      centres.headOption.toSuccessNel("list not expected to be empty")
    }
  }

  def withStudy(studyId: StudyId): Set[Centre] = {
    getValues.filter { c => c.studyIds.contains(studyId) }.toSet
  }

  def getByNames(names: Set[String]): Set[Centre] = {
    getValues.
      filter { centre =>
        names.contains(centre.name)
      }.
      toSet
  }

  testData.testCentres.foreach(put)
}
