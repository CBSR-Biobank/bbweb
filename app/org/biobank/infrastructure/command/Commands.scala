package org.biobank.infrastructure.command

import org.biobank.infrastructure._

object Commands {

  trait Command

  trait HasExpectedVersion { val expectedVersion: Option[Long] }

}

