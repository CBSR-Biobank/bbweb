package org.biobank.service

import akka.actor.ActorLogging
import akka.persistence.PersistentActor
import com.trueaccord.scalapb.GeneratedMessage
import org.biobank.domain._
import scalaz.Scalaz._

trait Processor extends PersistentActor with ActorLogging {
  import org.biobank.CommonValidations._

  /**
   * Persists the event passed in the validation if it is successful. In either case
   * the sender is sent either the success or failure validation.
   *
   * @see http://helenaedelson.com/?p=879
   *
   * TODO: convert to single parameter list
   */
  protected def process[T <: GeneratedMessage](validation: ServiceValidation[T])(successFn: T => Unit): Unit = {
    val originalSender = context.sender
    validation.fold(
      err => {
        // inform the sender of the failure
        originalSender ! validation
      },
      event => {
        // FIXME: change this call to a peristAsync()?
        persist(event) { ev =>
          successFn(ev)
          // inform the sender of the successful event resulting from a valid command
          originalSender ! ev.successNel[String]
        }
      }
    )
  }

  protected def validNewIdentity[I <: IdentifiedValueObject[_], R <: ReadWriteRepository[I,_]](
    id: I, repository: R): ServiceValidation[I] = {
    if (repository.getByKey(id).isSuccess) {
      ServiceError(s"could not generate a unique ID: $id").failureNel[I]
    } else {
      id.successNel[String]
    }
  }

  /**
   * Searches the repository for a matching item.
   */
  protected def nameAvailableMatcher[T <: IdentifiedDomainObject[_]]
    (name: String, repository: ReadRepository[_, T], errMsgPrefix: String)
    (matcher: T => Boolean)
      : ServiceValidation[Boolean] = {
    val exists = repository.getValues.exists { item =>
      matcher(item)
    }
    if (exists) EntityCriteriaError(s"$errMsgPrefix: $name").failureNel[Boolean]
    else true.successNel[String]
  }

  /** Checks that the domain objects version matches the expected one.
    */
  protected def validateVersion[T <: ConcurrencySafeEntity[_]](
    item: T,
    expectedVersion: Option[Long]): ServiceValidation[Boolean] = {
    if (item.versionOption == expectedVersion) true.successNel[String]
    else ServiceError(s"version mismatch").failureNel[Boolean]
  }

}
