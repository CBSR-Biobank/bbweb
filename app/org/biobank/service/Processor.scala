package org.biobank.service

import org.biobank.infrastructure.event.Events._
import org.biobank.domain._

import akka.actor._
import org.slf4j.Logger
import akka.persistence.EventsourcedProcessor

import scalaz._
import Scalaz._

trait Processor extends EventsourcedProcessor with ActorLogging {

  /** Persists the event passed in the validation if it is successful. In either case
    *  the sender is sent either the success or failure validation.
    */
  protected def process[T](validation: DomainValidation[T])(onSuccess: T => Unit) {
    validation map { event =>
      persist(event) { e =>
	onSuccess(e)
	// inform the sender of the successful command with the event
	context.sender ! e.success
      }
    }

    if (validation.isFailure) {
      // inform the sender of the failure
      context.sender ! validation
    }
  }

  /**
    */
  protected  def nameAvailableMatcher[T <: ConcurrencySafeEntity[_]](
    name: String,
    repository: ReadRepository[_, T])(
    matcher: T => Boolean): DomainValidation[Boolean] = {
    val exists = repository.getValues.exists { item =>
      matcher(item)
    }
    if (exists) {
      DomainError(s"item with name already exists: $name").failNel
    } else {
      true.success
    }
  }

  protected  def validateVersion[T <: ConcurrencySafeEntity[_]](
    item: T,
    expectedVersion: Option[Long]): DomainValidation[Boolean] = {
    if (item.versionOption == expectedVersion) true.success
    else DomainError(s"version mismatch").failNel
  }

}
