package infrastructure.command

object Commands {

  trait Command

  trait HasExpectedVersion { val expectedVersion: Option[Long] }

}
