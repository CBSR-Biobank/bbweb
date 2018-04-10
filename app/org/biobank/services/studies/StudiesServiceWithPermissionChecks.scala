package org.biobank.services.studies

import org.biobank.domain.access._
import org.biobank.domain.users.UserId
import org.biobank.domain.studies._
import org.biobank.services._
import org.biobank.services.ServicePermissionChecks
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
    accessService.getUserMembership(userId).flatMap { membership =>
      if (membership.studyData.allEntities) {
        studyRepository.getValues.toSet.successNel[String]
      } else {
        membership.studyData.ids
          .map(studyRepository.getByKey)
          .toList.sequenceU
          .map(studies => studies.toSet)
      }
    }
  }

}
