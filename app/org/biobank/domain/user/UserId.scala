package org.biobank.domain.user

import org.biobank.domain.IdentifiedValueObject

case class UserId(id: String) extends IdentifiedValueObject[String] {}
