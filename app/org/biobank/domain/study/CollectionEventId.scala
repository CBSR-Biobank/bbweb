package org.biobank.domain.study

import org.biobank.domain.IdentifiedValueObject

case class CollectionEventId(val id: String) extends IdentifiedValueObject[String] {}
