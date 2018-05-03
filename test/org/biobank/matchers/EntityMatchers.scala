package org.biobank.matchers

import java.time.OffsetDateTime
import org.biobank.domain.{ConcurrencySafeEntity, Location}
import org.biobank.domain.annotations._
import org.biobank.domain.centres._
import org.biobank.domain.studies._
import org.biobank.domain.users._
import org.scalatest.matchers.{MatchResult, Matcher}

trait EntityMatchers {

  import DateMatchers._

  def beEntityWithTimeAddedWithinSeconds(time: OffsetDateTime, diffSeconds: Long) =
    beTimeWithinSeconds(time, diffSeconds) compose { (e: ConcurrencySafeEntity[_]) => e.timeAdded }

  def beEntityWithTimeModifiedWithinSeconds(time: Option[OffsetDateTime], diffSeconds: Long) =
    beOptionalTimeWithinSeconds(time, diffSeconds) compose {
      (e: ConcurrencySafeEntity[_]) => e.timeModified
    }

  def beEntityWithTimeStamps[T <: ConcurrencySafeEntity[_]](timeAdded:    OffsetDateTime,
                                                            timeModified: Option[OffsetDateTime],
                                                            diffSeconds:  Long) =
    new Matcher[T] {
      def apply(left: T) = {
        val timeAddedMatches = beEntityWithTimeAddedWithinSeconds(timeAdded, diffSeconds)(left)
        if (!timeAddedMatches.matches) {
          MatchResult(false,
                      "timeAdded exceeds difference of {0} seconds: timeAdded: {1}, expected: {2}",
                      "timeAdded within difference of {0} seconds: timeAdded: {1}, expected: {2}",
                      IndexedSeq(diffSeconds, left.timeAdded, timeAdded))
        } else {
          val timeModifiedMatches = beEntityWithTimeModifiedWithinSeconds(timeModified, diffSeconds)(left)
          MatchResult(timeModifiedMatches.matches,
                      s"timeModified: ${timeModifiedMatches.failureMessage}",
                      s"timeModified: ${timeModifiedMatches.negatedFailureMessage}")
        }
      }
    }

  /**
   * This matcher allows for time differences in `timeAdded` and `timeModified` of 5 seconds.
   *
   * The `equals` matcher, from scalatest, cannot be used since ConcurrencySafeEntity overrides `equals`
   * and `hashCode`.
   */
  def matchCentre(centre: Centre) =
    new Matcher[Centre] {
      def apply(left: Centre) = {
        val matchers = Map(
            ("id"          -> (left.id equals centre.id)),
            ("slug"        -> (left.slug equals centre.slug)),
            ("state"       -> (left.state equals centre.state)),
            ("name"        -> (left.name equals centre.name)),
            ("description" -> (left.description equals centre.description)),
            ("studyIds"    -> (left.studyIds equals centre.studyIds)),
            ("locations"   -> (locationsMatch(left.locations, centre.locations)))) ++
        entitiesAttrsMatch(centre, left)

        val nonMatching = matchers filter { case (k, v) => !v } keys

        MatchResult(nonMatching.size <= 0,
                    "centres do not match for the folowing attributes: {0},\n: actual {1},\nexpected: {2}",
                    "centres match: actual: {1},\nexpected: {2}",
                    IndexedSeq(nonMatching.mkString(", "), left, centre))
      }
    }

  /**
   * This matcher allows for time differences in `timeAdded` and `timeModified` of 5 seconds.
   *
   * The `equals` matcher, from scalatest, cannot be used since ConcurrencySafeEntity overrides `equals`
   * and `hashCode`.
   */
  def matchStudy(study: Study) =
    new Matcher[Study] {
      def apply(left: Study) = {
        val matchers = Map(
            ("id"              -> (left.id equals study.id)),
            ("slug"            -> (left.slug equals study.slug)),
            ("state"           -> (left.state equals study.state)),
            ("name"            -> (left.name equals study.name)),
            ("description"     -> (left.description equals study.description)),
            ("annotationTypes" -> (annotationTypesMatch(left.annotationTypes, study.annotationTypes)))) ++
        entitiesAttrsMatch(study, left)

        val nonMatching = matchers filter { case (k, v) => !v } keys

        MatchResult(nonMatching.size <= 0,
                    "studies do not match for the folowing attributes: {0},\n: actual {1},\nexpected: {2}",
                    "studies match: actual: {1},\nexpected: {2}",
                    IndexedSeq(nonMatching.mkString(", "), left, study))
      }
    }

  /**
   * This matcher allows for time differences in `timeAdded` and `timeModified` of 5 seconds.
   *
   * The `equals` matcher, from scalatest, cannot be used since ConcurrencySafeEntity overrides `equals`
   * and `hashCode`.
   */
  def matchCollectionEventType(eventType: CollectionEventType) =
    new Matcher[CollectionEventType] {
      def apply(left: CollectionEventType) = {
        val matchers = Map(
            ("id"              -> (left.id equals eventType.id)),
            ("slug"            -> (left.slug equals eventType.slug)),
            ("name"            -> (left.name equals eventType.name)),
            ("description"     -> (left.description equals eventType.description)),
            ("recurring"       -> (left.recurring equals eventType.recurring)),
            ("specimenDefinitions" -> collectionSpecimenDefinitionsMatch(left.specimenDefinitions,
                                                                        eventType.specimenDefinitions)),
            ("annotationTypes" -> (annotationTypesMatch(left.annotationTypes,
                                                       eventType.annotationTypes)))) ++
        entitiesAttrsMatch(eventType, left)

        val nonMatching = matchers filter { case (k, v) => !v } keys

        MatchResult(nonMatching.size <= 0,
                    "event types do not match for the folowing attributes: {0},\n: actual {1},\nexpected: {2}",
                    "event types match: actual: {1},\nexpected: {2}",
                    IndexedSeq(nonMatching.mkString(", "), left, eventType))
      }
    }

  /**
   * This matcher allows for time differences in `timeAdded` and `timeModified` of 5 seconds.
   *
   * The `equals` matcher, from scalatest, cannot be used since ConcurrencySafeEntity overrides `equals`
   * and `hashCode`.
   */
  def matchUser(user: User) =
    new Matcher[User] {
      def apply(left: User) = {
        val matchers = Map(
            ("id"              -> (left.id equals user.id)),
            ("slug"            -> (left.slug equals user.slug)),
            ("state"           -> (left.state equals user.state)),
            ("name"         -> (left.name equals user.name)),
            ("email"        -> (left.email equals user.email)),
            ("avatarUrl"    -> (left.avatarUrl equals user.avatarUrl)))
       entitiesAttrsMatch(user, left)

        val nonMatching = matchers filter { case (k, v) => !v } keys

        MatchResult(nonMatching.size <= 0,
                    "users do not match for the following attributes: {0},\n: actual {1},\nexpected: {2}",
                    "users match: actual: {1},\nexpected: {2}",
                    IndexedSeq(nonMatching.mkString(", "), left, user))
      }
    }

  // def optionalStringMatcher(strMaybe: Option[String]) =
  //   new Matcher[Option[String]] {
  //     def apply(left: Option[String]) =
  //     (left, strMaybe) match {
  //       case (Some(leftStr), Some(str)) =>
  //         MatchResult(leftStr == str,
  //                     "strings do not match: expected {0}, actual: {1}",
  //                     "strings do match: expected {0}, actual: {1}",
  //                     IndexedSeq(str, leftStr))
  //       case (None, Some(time)) =>
  //         MatchResult(false, "expected is None and actual is not None", "")
  //       case (Some(leftTime), None) =>
  //         MatchResult(false, "actual is None and expected is not None", "")
  //       case (None, None) =>
  //         MatchResult(true,
  //                     "actual and expected time are both None",
  //                     "actual and expected time are both None")
  //     }
  //   }

  private def annotationTypesMatch(a: Set[AnnotationType], b: Set[AnnotationType]): Boolean = {
    val maybeMatch = a.map { atToFind =>
        b.exists { atOther =>
          ((atOther.id            equals atToFind.id) &&
             (atOther.slug          equals atToFind.slug) &&
             (atOther.name          equals atToFind.name) &&
             (atOther.description   equals atToFind.description) &&
             (atOther.valueType     equals atToFind.valueType) &&
             (atOther.maxValueCount equals atToFind.maxValueCount) &&
             (atOther.options       equals atToFind.options) &&
             (atOther.required      equals atToFind.required))
        }
      }

    maybeMatch.foldLeft(true) { (result, found) => result && found }
  }

  private def collectionSpecimenDefinitionsMatch(a: Set[CollectionSpecimenDefinition],
                                                 b: Set[CollectionSpecimenDefinition]): Boolean = {
    val maybeMatch = a.map { toFind =>
        b.exists { other =>
          ((other.id            equals toFind.id) &&
             (other.slug          equals toFind.slug) &&
             (other.name          equals toFind.name) &&
             (other.description   equals toFind.description) &&
             (other.units                   equals toFind.units) &&
             (other.anatomicalSourceType    equals toFind.anatomicalSourceType) &&
             (other.preservationType        equals toFind.preservationType) &&
             (other.preservationTemperature equals toFind.preservationTemperature) &&
             (other.specimenType            equals toFind.specimenType) &&
             (other.maxCount                equals toFind.maxCount) &&
             (other.amount                  equals toFind.amount))
        }
      }

    maybeMatch.foldLeft(true) { (result, found) => result && found }
  }

  private def locationsMatch(a: Set[Location], b: Set[Location]): Boolean = {
    val maybeMatch = a.map { toFind =>
        b.exists { other =>
          ((other.id               equals toFind.id) &&
             (other.slug           equals toFind.slug) &&
             (other.name           equals toFind.name) &&
             (other.street         equals toFind.street) &&
             (other.city           equals toFind.city) &&
             (other.province       equals toFind.province) &&
             (other.postalCode     equals toFind.postalCode) &&
             (other.poBoxNumber    equals toFind.poBoxNumber) &&
             (other.countryIsoCode equals toFind.countryIsoCode))
        }
      }

    maybeMatch.foldLeft(true) { (result, found) => result && found }
  }

  private def entitiesAttrsMatch(a: ConcurrencySafeEntity[_], b: ConcurrencySafeEntity[_]) = {
    val timeAddedMatcher = beTimeWithinSeconds(a.timeAdded, 5L)(b.timeAdded)

    val timeModifiedMatcher = beOptionalTimeWithinSeconds(a.timeModified, 5L)(b.timeModified)

    Map(("version"      -> (a.version equals b.version)),
        ("timeAdded"    -> (timeAddedMatcher.matches)),
        ("timeModified" -> (timeModifiedMatcher.matches)))
  }

}

object EntityMatchers extends EntityMatchers
