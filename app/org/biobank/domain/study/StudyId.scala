package org.biobank.domain.study

import org.biobank.domain.IdentifiedValueObject

case class StudyId(val id: String) extends IdentifiedValueObject[String] {}
