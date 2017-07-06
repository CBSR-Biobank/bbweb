package org.biobank.domain.access

import java.time.OffsetDateTime
import org.biobank.domain._
import org.biobank.domain.user.{User, UserId}
import org.biobank.domain.study.StudyId
import org.biobank.domain.centre.CentreId
import org.slf4j.{Logger, LoggerFactory}
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

final case class MembershipStudyInfo(allStudies: Boolean, studyIds: Set[StudyId]) {

  def hasAllStudies(): MembershipStudyInfo =
    copy(allStudies = true, studyIds = Set.empty[StudyId])

 def addStudy(id: StudyId): MembershipStudyInfo =
    copy(allStudies = false, studyIds = studyIds + id)

 def removeStudy(id: StudyId): MembershipStudyInfo =
   copy(allStudies = false, studyIds = studyIds - id)

  def isMemberOfStudy(id: StudyId): Boolean = {
    if (allStudies) true
    else studyIds.exists(_ == id)
  }

}

final case class MembershipCentreInfo(allCentres: Boolean, centreIds: Set[CentreId]) {

  def hasAllCentres(): MembershipCentreInfo =
    copy(allCentres = true, centreIds = Set.empty[CentreId])

 def addCentre(id: CentreId): MembershipCentreInfo =
   copy(allCentres = false, centreIds = centreIds + id)

 def removeCentre(id: CentreId): MembershipCentreInfo =
    copy(allCentres = false, centreIds = centreIds - id)

  def isMemberOfCentre(id: CentreId): Boolean = {
    if (allCentres) true
    else centreIds.exists(_ == id)
  }
}

final case class Membership(id:           MembershipId,
                            version:      Long,
                            timeAdded:    OffsetDateTime,
                            timeModified: Option[OffsetDateTime],
                            userIds:      Set[UserId],
                            studyInfo:    MembershipStudyInfo,
                            centreInfo:   MembershipCentreInfo)
    extends ConcurrencySafeEntity[MembershipId] {

  val log: Logger = LoggerFactory.getLogger(this.getClass)

  def addUser(user: User): Membership = {
    copy(userIds      = userIds + user.id,
         version      = version + 1,
         timeModified = Some(OffsetDateTime.now))
  }

  def hasAllStudies(): Membership = {
    copy(studyInfo    = studyInfo.hasAllStudies,
         version      = version + 1,
         timeModified = Some(OffsetDateTime.now))
  }

  def addStudy(id: StudyId): Membership = {
    copy(studyInfo    = studyInfo.addStudy(id),
         version      = version + 1,
         timeModified = Some(OffsetDateTime.now))
  }

  def removeStudy(id: StudyId): Membership = {
    copy(studyInfo    = studyInfo.removeStudy(id),
         version      = version + 1,
         timeModified = Some(OffsetDateTime.now))
  }

  def hasAllCentres(setting: Boolean): Membership = {
    copy(centreInfo   = centreInfo.hasAllCentres,
         version      = version + 1,
         timeModified = Some(OffsetDateTime.now))
  }

  def addCentre(id: CentreId): Membership = {
    copy(centreInfo   = centreInfo.addCentre(id),
         version      = version + 1,
         timeModified = Some(OffsetDateTime.now))
  }

  def removeCentre(id: CentreId): Membership = {
    copy(centreInfo   = centreInfo.removeCentre(id),
         version      = version + 1,
         timeModified = Some(OffsetDateTime.now))
  }

  /**
   * If studyId is None, then don't bother checking for study membership.
   */
  def isMemberOfStudy(id: StudyId): Boolean = {
    studyInfo.isMemberOfStudy(id)
  }

  /**
   * If centreId is None, then don't bother checking for study membership.
   */
  def isMemberOfCentre(id: CentreId): Boolean = {
    centreInfo.isMemberOfCentre(id)
  }

  /**
   * If studyId and centreId are None, then don't bother checking for membership.
   */
  def isMember(studyId: Option[StudyId], centreId: Option[CentreId]): Boolean = {
    (studyId, centreId) match {
      case (None, None)           => true
      case (Some(studyId), None)  => isMemberOfStudy(studyId)
      case (None, Some(centreId)) => isMemberOfCentre(centreId)
      case (Some(sId), Some(cId)) => isMemberOfStudy(sId) && isMemberOfCentre(cId)
    }
  }

  override def toString: String =
    s"""|Membership:{
        |  id:           $id
        |  version:      $version
        |  timeAdded:    $timeAdded
        |  timeModified: $timeModified
        |  userIds:      $userIds
        |  studyInfo:    $studyInfo
        |  centreInfo:   $centreInfo
        |}""".stripMargin
}

object Membership {
  import org.biobank.domain.CommonValidations._

  case object InvalidMembershipId extends org.biobank.ValidationKey

  def create(id:           MembershipId,
             version:      Long,
             timeAdded:    OffsetDateTime,
             timeModified: Option[OffsetDateTime],
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
                   studyInfo     = MembershipStudyInfo(allStudies, studyIds),
                   centreInfo    = MembershipCentreInfo(allCentres, centreIds))
    }
  }

  implicit val membershipStudyIds: Format[MembershipStudyInfo] = Json.format[MembershipStudyInfo]

  implicit val membershipCentreIds: Format[MembershipCentreInfo] = Json.format[MembershipCentreInfo]

  implicit val membershipFormat: Format[Membership] = Json.format[Membership]

}
