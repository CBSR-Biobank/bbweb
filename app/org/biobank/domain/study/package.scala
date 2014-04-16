package org.biobank.domain

package object study {

  trait HasStudyId {

    /** The ID of the study this object belongs to. */
    val studyId: StudyId

  }

}
