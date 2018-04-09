package org.biobank.domain.access

import java.time.OffsetDateTime
import org.biobank.domain._
import org.biobank.domain.users.UserId
import org.biobank.domain.studies.StudyId
import org.biobank.domain.centres.CentreId
import play.api.libs.json._
import play.api.libs.functional.syntax._
import scalaz.Scalaz._

/**
 * Predicates that can be used to filter collections of memberships.
 *
 */
trait MembershipPredicates extends HasNamePredicates[Membership] {

  type MembershipFilter = Membership => Boolean

}

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

/**
 * Used to track the IDs of the entities a Membership is for.
 *
 */
final case class MembershipEntitySet[T <: IdentifiedValueObject[_]](allEntities: Boolean, ids: Set[T]) {

  def hasAllEntities(): MembershipEntitySet[T] =
    copy(allEntities = true, ids = Set.empty[T])

 def addEntity(id: T): MembershipEntitySet[T] =
    copy(allEntities = false, ids = ids + id)

 def removeEntity(id: T): MembershipEntitySet[T] =
   copy(allEntities = false, ids = ids - id)

  def isMemberOf(id: T): Boolean = {
    if (allEntities) true
    else ids.exists(_ == id)
  }

}

object MembershipEntitySet {

  implicit def format[T <: IdentifiedValueObject[_]](implicit fmt: Format[T])
      : Format[MembershipEntitySet[T]] =
    ((__ \ "allEntities").format[Boolean] ~
       (__ \ "ids").format[Set[T]]
    )(MembershipEntitySet.apply, unlift(MembershipEntitySet.unapply))

}

sealed trait MembershipBase
    extends ConcurrencySafeEntity[MembershipId]
    with HasUniqueName
    with HasSlug
    with HasOptionalDescription {
  val id:           MembershipId
  val version:      Long
  val timeAdded:    OffsetDateTime
  val timeModified: Option[OffsetDateTime]
  val studyData:    MembershipEntitySet[StudyId]
  val centreData:   MembershipEntitySet[CentreId]

  /**
   * If studyId is None, then don't bother checking for study membership.
   */
  def isMemberOfStudy(id: StudyId): Boolean = {
    studyData.isMemberOf(id)
  }

  /**
   * If centreId is None, then don't bother checking for study membership.
   */
  def isMemberOfCentre(id: CentreId): Boolean = {
    centreData.isMemberOf(id)
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
}

trait MembershipValidations {

  val NameMinLength: Long = 2L

}

final case class Membership(id:           MembershipId,
                            version:      Long,
                            timeAdded:    OffsetDateTime,
                            timeModified: Option[OffsetDateTime],
                            slug: Slug,
                            name:         String,
                            description:  Option[String],
                            userIds:      Set[UserId],
                            studyData:    MembershipEntitySet[StudyId],
                            centreData:   MembershipEntitySet[CentreId])
    extends MembershipBase
    with MembershipValidations {
  import org.biobank.CommonValidations._
  import org.biobank.domain.DomainValidations._

  /** Used to change the name. */
  def withName(name: String): DomainValidation[Membership] = {
    validateString(name, NameMinLength, InvalidName) map { _ =>
      copy(name         = name,
           version      = version + 1,
           timeModified = Some(OffsetDateTime.now))
    }
  }

  /** Used to change the description. */
  def withDescription(description: Option[String]): DomainValidation[Membership] = {
    validateNonEmptyStringOption(description, InvalidDescription) map { _ =>
      copy(description  = description,
           version      = version + 1,
           timeModified = Some(OffsetDateTime.now))
    }
  }

  def addUser(userId: UserId): Membership = {
    copy(userIds      = userIds + userId,
         version      = version + 1,
         timeModified = Some(OffsetDateTime.now))
  }

  def removeUser(id: UserId): Membership = {
    copy(userIds      = userIds - id,
         version      = version + 1,
         timeModified = Some(OffsetDateTime.now))
  }

  def hasAllStudies(): Membership = {
    copy(studyData    = studyData.hasAllEntities,
         version      = version + 1,
         timeModified = Some(OffsetDateTime.now))
  }

  def addStudy(id: StudyId): Membership = {
    copy(studyData    = studyData.addEntity(id),
         version      = version + 1,
         timeModified = Some(OffsetDateTime.now))
  }

  def removeStudy(id: StudyId): Membership = {
    copy(studyData    = studyData.removeEntity(id),
         version      = version + 1,
         timeModified = Some(OffsetDateTime.now))
  }

  def hasAllCentres(): Membership = {
    copy(centreData   = centreData.hasAllEntities,
         version      = version + 1,
         timeModified = Some(OffsetDateTime.now))
  }

  def addCentre(id: CentreId): Membership = {
    copy(centreData   = centreData.addEntity(id),
         version      = version + 1,
         timeModified = Some(OffsetDateTime.now))
  }

  def removeCentre(id: CentreId): Membership = {
    copy(centreData   = centreData.removeEntity(id),
         version      = version + 1,
         timeModified = Some(OffsetDateTime.now))
  }

  override def toString: String =
    s"""|Membership:{
        |  id:           $id
        |  version:      $version
        |  timeAdded:    $timeAdded
        |  timeModified: $timeModified
        |  slug:         $slug,
        |  name:         $name
        |  description:  $description
        |  userIds:      $userIds
        |  studyData:    $studyData
        |  centreData:   $centreData
        |}""".stripMargin
}

object Membership extends MembershipValidations {
  import org.biobank.CommonValidations._
  import org.biobank.domain.DomainValidations._

  case object InvalidMembershipId extends org.biobank.ValidationKey

  def create(id:           MembershipId,
             version:      Long,
             timeAdded:    OffsetDateTime,
             timeModified: Option[OffsetDateTime],
             name:         String,
             description:  Option[String],
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
       validateString(name, NameMinLength, InvalidName) |@|
       validateNonEmptyStringOption(description, InvalidDescription) |@|
       userIds.map(validateId(_, InvalidUserId)).toList.sequenceU |@|
       studyIds.map(validateId(_, InvalidStudyId)).toList.sequenceU |@|
       centreIds.map(validateId(_, InvalidCentreId)).toList.sequenceU |@|
       checkAllStudies  |@|
       checkAllCentres) { case _ =>
        Membership(id           = id,
                   version      = version,
                   timeAdded    = timeAdded,
                   timeModified = timeModified,
                   slug         = Slug(name),
                   name         = name,
                   description  = description,
                   userIds      = userIds,
                   studyData    = MembershipEntitySet[StudyId](allStudies, studyIds),
                   centreData   = MembershipEntitySet[CentreId](allCentres, centreIds))
    }
  }

  implicit val usersMembershipformat: Format[Membership] = Json.format[Membership]

  val sort2Compare: Map[String, (Membership, Membership) => Boolean] =
    Map[String, (Membership, Membership) => Boolean]("name"  -> compareByName)

  def compareByName(a: Membership, b: Membership): Boolean = {
    (a.name compareToIgnoreCase b.name) < 0
  }}

/**
 * The membership belonging to a single user.
 *
 * This class has no information as to what other users share this membership.
 */
final case class UserMembership(id:           MembershipId,
                                version:      Long,
                                timeAdded:    OffsetDateTime,
                                timeModified: Option[OffsetDateTime],
                                slug: Slug,
                                name:         String,
                                description:  Option[String],
                                userId:       UserId,
                                studyData:    MembershipEntitySet[StudyId],
                                centreData:   MembershipEntitySet[CentreId])
    extends MembershipBase
    with MembershipValidations {

  override def toString: String =
    s"""|UserMembership:{
        |  id:           $id
        |  version:      $version
        |  timeAdded:    $timeAdded
        |  timeModified: $timeModified
        |  slug:         $slug,
        |  name:         $name
        |  description:  $description
        |  userId:       $userId
        |  studyData:    $studyData
        |  centreData:   $centreData
        |}""".stripMargin

}

object UserMembership {

  def create(usersMembership: Membership, userId: UserId): UserMembership = {
    UserMembership(id           = usersMembership.id,
                   version      = usersMembership.version,
                   timeAdded    = usersMembership.timeAdded,
                   timeModified = usersMembership.timeModified,
                   slug         = usersMembership.slug,
                   name         = usersMembership.name,
                   description  = usersMembership.description,
                   userId       = userId,
                   studyData    = usersMembership.studyData,
                   centreData   = usersMembership.centreData)
  }

  implicit val userMembershipformat: Format[UserMembership] = Json.format[UserMembership]

}
