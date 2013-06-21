package infrastructure.commands

trait Command {}

trait Identity { val id: String }

trait ExpectedVersion { val expectedVersion: Option[Long] }