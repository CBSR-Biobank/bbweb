package org.biobank.matchers

import java.time.{Duration, OffsetDateTime}
import org.scalatest.matchers.{MatchResult, Matcher}

trait DateMatchers {

  def beTimeWithinSeconds(time: OffsetDateTime, diffSeconds: Long): Matcher[OffsetDateTime] =
    new TimeWithinSeconds(time, diffSeconds)

  def beOptionalTimeWithinSeconds(time: Option[OffsetDateTime], diffSeconds: Long = 0L)
      : Matcher[Option[OffsetDateTime]] =
    new TimeOptionWithinSeconds(time, diffSeconds)

  private class TimeWithinSeconds(time: OffsetDateTime, diffSeconds: Long) extends Matcher[OffsetDateTime] {
    override def apply(left: OffsetDateTime) = {
      val timediff = Duration.between(left, time).getSeconds.abs

      MatchResult(timediff < diffSeconds,
                  "actual {0} and expected {1} differ by more than {2} seconds",
                  "actual {0} and expected {1} differ by less than {2} seconds",
                  IndexedSeq(left, time, diffSeconds))
    }
  }

  private class TimeOptionWithinSeconds(timeMaybe: Option[OffsetDateTime], diffSeconds: Long)
      extends Matcher[Option[OffsetDateTime]] {
    override def apply(left: Option[OffsetDateTime]) = {
      left match {
        case Some(leftTime) =>
          timeMaybe match {
            case Some(time) =>
              new TimeWithinSeconds(time, diffSeconds).apply(leftTime)
            case None =>
              MatchResult(false, "expected time is None and actual is not None", "")
          }
        case None =>
          timeMaybe match {
            case Some(actualTime) =>
              MatchResult(false, "actual time is None and expected is not None", "")
            case None =>
              MatchResult(true, "actual time and expected time are both None", "")
          }
      }
    }
  }

}

object DateMatchers extends DateMatchers
