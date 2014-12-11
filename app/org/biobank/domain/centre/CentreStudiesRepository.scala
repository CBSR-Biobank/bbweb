package org.biobank.domain.centre

import org.biobank.domain._
import org.biobank.domain.centre._
import org.biobank.domain.study.StudyId

import scalaz._
import Scalaz._

/** This repository maintains the relationship between a single centre and its multiple locations.
  */
trait CentreStudiesRepository extends ReadWriteRepository[StudyCentreId, StudyCentre] {

  def withIds(studyId: StudyId, centreId: CentreId): DomainValidation[StudyCentre]

  def withCentreId(centreId: CentreId): Set[StudyCentre]

}

class CentreStudiesRepositoryImpl
    extends ReadWriteRepositoryRefImpl[StudyCentreId, StudyCentre](v => v.id)
    with CentreStudiesRepository {

  def nextIdentity: StudyCentreId = StudyCentreId(nextIdentityAsString)

  def withIds(studyId: StudyId, centreId: CentreId): DomainValidation[StudyCentre] = {
    val option = getValues.find { x =>
      (x.centreId == centreId) && (x.studyId == studyId)
    }
    option.fold {
      DomainError(s"centre and study not linked: { centreId: $centreId, studyId: $studyId }")
        .failureNel[StudyCentre]
    } { value  =>
      value.successNel
    }
  }

  def withCentreId(centreId: CentreId): Set[StudyCentre] = {
    getValues.filter(x => x.centreId == centreId).toSet
  }

}
