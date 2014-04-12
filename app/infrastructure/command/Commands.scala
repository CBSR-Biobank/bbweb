package infrastructure.command

import infrastructure._

object Commands {

  trait Command

  trait HasExpectedVersion { val expectedVersion: Option[Long] }

}

