package org.biobank.domain.participant

import org.biobank.domain.IdentifiedValueObject

case class ParticipantId(val id: String) extends IdentifiedValueObject[String] {}