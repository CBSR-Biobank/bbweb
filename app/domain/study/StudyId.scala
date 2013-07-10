package domain.study

import domain.IdentifiedValueObject

case class StudyId(val id: String) extends IdentifiedValueObject[String] {}
