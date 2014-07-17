package org.biobank.domain.centre

import org.biobank.domain.study.StudyId

/** Used to link a study to a center.
  *
  * This is a many to many relationship.
  */
case class StudyCentre(id: StudyCentreId, studyId: StudyId, centreId: CentreId)
