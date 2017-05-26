package org.biobank.service.studies

import org.biobank.domain.access._
import org.biobank.domain.user.UserId
import org.biobank.domain.study._
import org.biobank.service._
import org.biobank.service.ServicePermissionChecks
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

trait StudyServicePermissionChecks extends ServicePermissionChecks {

  protected val studyRepository: StudyRepository

  protected def withPermittedStudies[T](requestUserId: UserId)(block: Set[Study] => ServiceValidation[T])
      : ServiceValidation[T] = {
    whenPermitted(requestUserId, PermissionId.StudyRead) { () =>
      for {
        studies <- getMembershipStudies(requestUserId)
        result  <- block(studies)
      } yield result
    }
  }

  protected def getMembershipStudies(userId: UserId): ServiceValidation[Set[Study]] = {
    accessService.getMembership(userId).flatMap { membership =>
      if (membership.studyInfo.allStudies) {
        studyRepository.getValues.toSet.successNel[String]
      } else {
        membership.studyInfo.studyIds
          .map(studyRepository.getByKey)
          .toList.sequenceU
          .map(studies => studies.toSet)
      }
    }
  }

}
