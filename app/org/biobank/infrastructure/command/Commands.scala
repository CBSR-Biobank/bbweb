package org.biobank.infrastructure.command

object Commands {

  trait Command {
    val userId: Option[String]
  }

  trait HasExpectedVersion {

    /** A command that must include the version of the object the command applies to. */
    val expectedVersion: Long
  }

}

