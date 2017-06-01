package org.biobank.service.centres

import org.biobank.domain.access._
import org.biobank.domain.user.UserId
import org.biobank.domain.centre._
import org.biobank.service._
import org.biobank.service.ServicePermissionChecks
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

trait CentreServicePermissionChecks extends ServicePermissionChecks {

  protected val centreRepository: CentreRepository

  protected def withPermittedCentres[T](requestUserId: UserId)(block: Set[Centre] => ServiceValidation[T])
      : ServiceValidation[T] = {
    whenPermitted(requestUserId, PermissionId.CentreRead) { () =>
      for {
        centres <- getMembershipCentres(requestUserId)
        result  <- block(centres)
      } yield result
    }
  }

  protected def getMembershipCentres(userId: UserId): ServiceValidation[Set[Centre]] = {
    accessService.getMembership(userId).flatMap { membership =>
      if (membership.centreInfo.allCentres) {
        centreRepository.getValues.toSet.successNel[String]
      } else {
        membership.centreInfo.centreIds
          .map(centreRepository.getByKey)
          .toList.sequenceU
          .map(centres => centres.toSet)
      }
    }
  }

}
