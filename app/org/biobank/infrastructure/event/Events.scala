package org.biobank.infrastructure.event

object Events {

  trait Event

  trait HasVersion {

    /** An event that includes the version of the object it references. */
    val version: Long

  }

}

