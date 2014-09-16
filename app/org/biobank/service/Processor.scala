package org.biobank.service

import org.biobank.infrastructure.event.Events._
import org.biobank.domain._

import akka.actor.ActorLogging
import org.slf4j.Logger
import akka.persistence.PersistentActor

import scalaz._
import scalaz.Scalaz._

trait Processor extends PersistentActor with ActorLogging {

  /** Persists the event passed in the validation if it is successful. In either case
    * the sender is sent either the success or failure validation.
    *
    * @see http://helenaedelson.com/?p=879
    */
  protected def process[T](validation: DomainValidation[T])(successFn: T => Unit) {
    val originalSender = context.sender
    validation map { event =>
      persist(event) { e =>
        successFn(e)
        // inform the sender of the successful command with the event
        originalSender ! e.success
      }
    }

    if (validation.isFailure) {
      // inform the sender of the failure
      originalSender ! validation
    }
  }

  /** Searches the repository for a matching item.
    */
  protected def nameAvailableMatcher[T <: ConcurrencySafeEntity[_]](
    name: String,
    repository: ReadRepository[_, T],
    errMsgPrefix: String)(
    matcher: T => Boolean): DomainValidation[Boolean] = {
    val exists = repository.getValues.exists { item =>
      matcher(item)
    }
    if (exists) {
      DomainError(s"$errMsgPrefix: $name").failNel
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
    else DomainError(s"version mismatch").failNel
  }

}
