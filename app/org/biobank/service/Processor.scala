package org.biobank.service

import org.biobank.domain._
import org.biobank.domain.user.UserId

import akka.actor.ActorLogging
import org.slf4j.Logger
import akka.persistence.PersistentActor
import org.joda.time.DateTime

import com.trueaccord.scalapb.GeneratedMessage

import scalaz._
import scalaz.Scalaz._

trait Processor extends PersistentActor with ActorLogging {

  /** Persists the event passed in the validation if it is successful. In either case
    * the sender is sent either the success or failure validation.
    *
    * @see http://helenaedelson.com/?p=879
    */
  protected def process[T <: GeneratedMessage]
    (validation: DomainValidation[T])
    (successFn: WrappedEvent[T] => Unit)
    (implicit userId: Option[UserId]) {
    val originalSender = context.sender
    validation.fold(
      err => {
        // inform the sender of the failure
        originalSender ! validation
      },
      event => {
        val wrappedEvent = WrappedEvent(event, userId, DateTime.now)
        // FIXME: change this call to a peristAsync()?
        persist(wrappedEvent) { we =>
          successFn(we)
          // inform the sender of the successful event resulting from a valid command
          originalSender ! we.event.success
        }
      }
    )
  }

  /** Searches the repository for a matching item.
    */
  protected def nameAvailableMatcher[T <: ConcurrencySafeEntity[_]]
    (name: String, repository: ReadRepository[_, T], errMsgPrefix: String)
    (matcher: T => Boolean)
      : DomainValidation[Boolean] = {
    val exists = repository.getValues.exists { item =>
      matcher(item)
    }
    if (exists) {
      DomainError(s"$errMsgPrefix: $name").failureNel
    } else {
      true.success
    }
  }

  /** Checks that the domain objects version matches the expected one.
    */
  protected def validateVersion[T <: ConcurrencySafeEntity[_]](
    item: T,
    expectedVersion: Option[Long]): DomainValidation[Boolean] = {
    if (item.versionOption == expectedVersion) true.success
    else DomainError(s"version mismatch").failureNel
  }

}
