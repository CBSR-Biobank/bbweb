package org.biobank.domain.participants

import org.biobank.ValidationKey
import org.biobank.domain.studies.CollectionEventTypeValidations

trait ParticipantValidations extends CollectionEventTypeValidations {

  case object UniqueIdInvalid extends ValidationKey

  case object UniqueIdRequired extends ValidationKey

  case object ParticipantIdRequired extends ValidationKey

  case object OriginLocationIdInvalid extends ValidationKey

  case object PositionInvalid extends ValidationKey

}
