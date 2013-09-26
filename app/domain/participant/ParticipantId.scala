package domain.participant

import domain.IdentifiedValueObject

case class ParticipantId(val id: String) extends IdentifiedValueObject[String] {}