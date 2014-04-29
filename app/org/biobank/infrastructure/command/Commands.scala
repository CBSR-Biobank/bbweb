package org.biobank.infrastructure.command

import org.biobank.infrastructure._

object Commands {

  trait Command

  trait HasExpectedVersion {

    /** A command that must include the version of the object the command applies to. */
    val expectedVersion: Option[Long]
  }

}

