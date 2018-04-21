package org.biobank.matchers

import org.biobank.domain.ConcurrencySafeEntity
import org.biobank.domain.annotations._
import org.biobank.domain.centres._
import org.biobank.domain.studies._
import org.biobank.dto.CentreDto
import java.time.OffsetDateTime
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
        val timeAddedMatches = beEntityWithTimeAddedWithinSeconds(timeAdded, diffSeconds).apply(left)
        if (!timeAddedMatches.matches) {
          MatchResult(false,
                      "timeAdded exceeds difference of {0} seconds: timeAdded: {1}, expected: {2}",
                      "timeAdded within difference of {0} seconds: timeAdded: {1}, expected: {2}",
                      IndexedSeq(diffSeconds, left.timeAdded, timeAdded))
        } else {
          val timeModifiedMatches =
            beEntityWithTimeModifiedWithinSeconds(timeModified, diffSeconds).apply(left)
          MatchResult(timeModifiedMatches.matches,
                      s"timeModified: ${timeModifiedMatches.failureMessage}",
                      s"timeModified: ${timeModifiedMatches.negatedFailureMessage}")
        }
      }
    }

  def matchCentre(centre: Centre) =
    new Matcher[CentreDto] {
      def apply(left: CentreDto) = {
        val dtoStudyIds = left.studyNames.map { s => StudyId(s.id)  }

        val timeAddedMatcher =
          beTimeWithinSeconds(centre.timeAdded, 5L)(OffsetDateTime.parse(left.timeAdded))

        val timeModifiedMatcher = beOptionalTimeWithinSeconds(centre.timeModified, 5L)
          .apply(left.timeModified.map(OffsetDateTime.parse))

        val matchers = Map(
            ("id"           -> (left.id equals centre.id.id)),
            ("version"      -> (left.version equals centre.version)),
            ("timeAdded"    -> (timeAddedMatcher.matches)),
            ("timeModified" -> (timeModifiedMatcher.matches)),
            ("slug"         -> (left.slug equals centre.slug)),
            ("state"        -> (left.state equals centre.state.id)),
            ("name"         -> (left.name equals centre.name)),
            ("description"  -> (left.description equals centre.description)),
            ("studyIds"     -> (dtoStudyIds equals centre.studyIds)),
            ("locations"    -> (left.locations equals centre.locations)))

        val nonMatching = matchers filter { case (k, v) => !v } keys

        MatchResult(nonMatching.size <= 0,
                    "dto does not match entity for the folowing attributes: {0},\ndto: {1},\nentity: {2}",
                    "dto matches entity: dto: {1},\nentity: {2}",
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

  private def entitiesAttrsMatch(a: ConcurrencySafeEntity[_], b: ConcurrencySafeEntity[_]) = {
    val timeAddedMatcher = beTimeWithinSeconds(a.timeAdded, 5L)(b.timeAdded)

    val timeModifiedMatcher = beOptionalTimeWithinSeconds(a.timeModified, 5L)(b.timeModified)

    Map(("version"      -> (a.version equals b.version)),
        ("timeAdded"    -> (timeAddedMatcher.matches)),
        ("timeModified" -> (timeModifiedMatcher.matches)))
  }

}

object EntityMatchers extends EntityMatchers
