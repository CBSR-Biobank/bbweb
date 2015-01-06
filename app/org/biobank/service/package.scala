package org.biobank

import org.biobank.domain.user.UserId
import org.biobank.infrastructure.command.Commands._
import org.joda.time.DateTime
import com.trueaccord.scalapb.GeneratedMessage

package  service {

  /** A wrapper for a command to include the user that issued it.
    *
    */
  case class WrappedCommand(command: Command, userId: Option[UserId])

  /** A wrapper for an event to include the user that issued it and the time that it was created.
    *
    */
  case class WrappedEvent[T <: GeneratedMessage](event: T, userId: Option[UserId], dateTime: DateTime);

}
