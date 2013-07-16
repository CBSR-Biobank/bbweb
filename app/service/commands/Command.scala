package service.commands

trait Command {}
trait HasExpectedVersion { val expectedVersion: Option[Long] }
