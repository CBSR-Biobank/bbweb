package org.biobank.domain

package study {

  trait HasStudyId {

    /** The ID of the study this entity belongs to. */
    val studyId: StudyId

  }

  trait HasSpecimenGroupId {

    /** The ID of the study this entity belongs to. */
    val specimenGroupId: SpecimenGroupId

  }

}
