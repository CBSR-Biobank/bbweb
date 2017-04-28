package org.biobank.domain.access

import org.biobank.domain._
import org.biobank.domain.user.{User, UserId}
import org.biobank.domain.study.{Study, StudyId}
import org.biobank.domain.centre.{Centre, CentreId}
import org.joda.time.DateTime
import play.api.libs.json._
import scalaz.Scalaz._

/** Identifies a unique [[Membership]] in the system.
  *
  * Used as a value object to maintain associations to with objects in the system.
  */
final case class MembershipId(id: String) extends IdentifiedValueObject[String]

object MembershipId {

  // Do not want JSON to create a sub object, we just want it to be converted
  // to a single string
  implicit val membershipIdReader: Reads[MembershipId] =
    (__).read[String].map( new MembershipId(_) )

  implicit val membershipIdWriter: Writes[MembershipId] =
    Writes{ (id: MembershipId) => JsString(id.id) }

}


final case class Membership(id:           MembershipId,
                            version:      Long,
                            timeAdded:    DateTime,
                            timeModified: Option[DateTime],
                            userIds:      Set[UserId],
                            allStudies:   Boolean,
                            allCentres:   Boolean,
                            studyIds:     Set[StudyId],
                            centreIds:    Set[CentreId])
    extends ConcurrencySafeEntity[MembershipId] {

  def addUser(user: User): Membership = {
    copy(userIds      = userIds + user.id,
         version      = version + 1,
         timeModified = Some(DateTime.now))
  }

  def hasAllStudies(setting: Boolean): Membership = {
    copy(allStudies   = setting,
         studyIds     = if (setting) Set.empty[StudyId] else studyIds,
         version      = version + 1,
         timeModified = Some(DateTime.now))
  }

  def addStudy(study: Study): Membership = {
    copy(studyIds     = studyIds + study.id,
         allStudies   = false,
         version      = version + 1,
         timeModified = Some(DateTime.now))
  }

  def removeStudy(study: Study): Membership = {
    copy(studyIds     = studyIds - study.id,
         version      = version + 1,
         timeModified = Some(DateTime.now))
  }

  def hasAllCentres(setting: Boolean): Membership = {
    copy(allCentres   = setting,
         centreIds    = if (setting) Set.empty[CentreId] else centreIds,
         version      = version + 1,
         timeModified = Some(DateTime.now))
  }

  def addCentre(centre: Centre): Membership = {
    copy(centreIds    = centreIds + centre.id,
         allCentres   = false,
         version      = version + 1,
         timeModified = Some(DateTime.now))
  }

  def removeCentre(centre: Centre): Membership = {
    copy(centreIds    = centreIds - centre.id,
         version      = version + 1,
         timeModified = Some(DateTime.now))
  }

  /**
   * If studyId is None, then don't bother checking for study membership.
   */
  def isMemberOfStudy(studyId: Option[StudyId]): Boolean = {
    studyId match {
      case None => true
      case _    => if (allStudies) true
                   else studyId.map(id => studyIds.exists(_ == id)).getOrElse(true)
    }
  }

  /**
   * If centreId is None, then don't bother checking for study membership.
   */
  def isMemberOfCentre(centreId: Option[CentreId]): Boolean = {
    if (allCentres) true
    else centreId.map(id => centreIds.exists(_ == id)).getOrElse(true)
  }

  /**
   * If studyId and centreId are None, then don't bother checking for membership.
   */
  def isMember(studyId: Option[StudyId], centreId: Option[CentreId]): Boolean = {
    (studyId, centreId) match {
      case (None, None) => true
      case _            => isMemberOfStudy(studyId) && isMemberOfCentre(centreId)
    }
  }
}

object Membership {
  import org.biobank.domain.CommonValidations._

  case object InvalidMembershipId extends org.biobank.ValidationKey

  def create(id:           MembershipId,
             version:      Long,
             timeAdded:    DateTime,
             timeModified: Option[DateTime],
             userIds:      Set[UserId],
             allStudies:   Boolean,
             allCentres:   Boolean,
             studyIds:     Set[StudyId],
             centreIds:    Set[CentreId]): DomainValidation[Membership] = {

    def checkAllStudies(): DomainValidation[Boolean] =
      if (allStudies && ! studyIds.isEmpty) DomainError("invalid studies for membership").failureNel[Boolean]
      else true.successNel[String]

    def checkAllCentres(): DomainValidation[Boolean] =
      if (allCentres && ! centreIds.isEmpty) DomainError("invalid centres for membership").failureNel[Boolean]
      else true.successNel[String]

    (validateId(id, InvalidMembershipId) |@|
       userIds.map(validateId(_, InvalidStudyId)).toList.sequenceU |@|
       studyIds.map(validateId(_, InvalidStudyId)).toList.sequenceU |@|
       centreIds.map(validateId(_, InvalidCentreId)).toList.sequenceU |@|
       checkAllStudies  |@|
       checkAllCentres) { case _ =>
        Membership(id           = id,
                   version      = version,
                   timeAdded    = timeAdded,
                   timeModified = timeModified,
                   userIds      = userIds,
                   allStudies   = allStudies,
                   allCentres   = allCentres,
                   studyIds     = studyIds,
                   centreIds    = centreIds)
    }
  }

  implicit val domainFormat: Format[Membership] = Json.format[Membership]

}
