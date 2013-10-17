import infrastructure._
import domain._
import domain.study._

import org.slf4j.Logger

import scalaz._
import scalaz.Scalaz._

/**
 * The service layer.
 *
 * Provides the API to the web application.
 */
package object service {

  /**
   * All Domain Services extend this trait.
   *
   * Aggregate roots can delegate commands to domain services. A domain service extends
   * this trait so that it can receive commands.
   */
  trait CommandHandler {

    /**
     * A partial function to handle a command. The input is a [[service.CommandMsg]]. If the
     * command is successful, an event is sent to the event bus and also returned as a
     * [[domain.DomainValidation]] object. If the command is invalid, then the error message
     * is returned in a [[domain.DomainValidation]] object.
     */
    type ProcessResult = PartialFunction[CommandMsg, DomainValidation[Any]]

    /**
     * The partial function that receives the event.
     *
     * @return a [[domain.DomainValidation]] object.
     *
     * @see [ProcessResult]
     */
    def process: ProcessResult

    /* Used to log the results of processing a command */
    protected def logMethod(
      log: Logger,
      methodName: String,
      cmd: Any,
      validation: DomainValidation[Any]) {
      if (log.isDebugEnabled) {
        log.debug("%s: %s".format(methodName, cmd))
        validation match {
          case Success(item) =>
            log.debug("%s: %s".format(methodName, item))
          case Failure(msglist) =>
            log.debug("%s: { msg: %s }".format(methodName, msglist.head))
        }
      }
    }

  }

  trait IdentityService {
    def nextIdentity: String = java.util.UUID.randomUUID.toString.toUpperCase
  }

  object StudyIdentityService extends IdentityService

  object SpecimenGroupIdentityService extends IdentityService

  object CollectionEventTypeIdentityService extends IdentityService

  object AnnotationTypeIdentityService extends IdentityService

  object SpecimenGroupCollectionEventTypeIdentityService extends IdentityService

  object CollectionEventAnnotationTypeIdentityService extends IdentityService

  object ParticipantAnnotationTypeIdentityService extends IdentityService

  object SpecimenLinkAnnotationTypeIdentityService extends IdentityService

  object UserIdentityService extends IdentityService

}

