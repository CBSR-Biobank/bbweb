package org.biobank.domain.participants

import org.biobank.ValidationKey

trait ParticipantValidations {

  case object UniqueIdInvalid extends ValidationKey

  case object UniqueIdRequired extends ValidationKey

  case object ParticipantIdRequired extends ValidationKey

  case object CollectionEventTypeIdRequired extends ValidationKey

  case object AmountInvalid extends ValidationKey

  case object OriginLocationIdInvalid extends ValidationKey

  case object PositionInvalid extends ValidationKey

}
