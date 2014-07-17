package org.biobank.infrastructure.command

import org.biobank.infrastructure._
import org.biobank.infrastructure.command.Commands._

object CentreCommands {
  trait CentreCommand extends Command
  trait HasCentreIdentity { val centreId: String }

  case class AddCentreCmd(
    name: String,
    description: Option[String] = None)
      extends CentreCommand

  case class UpdateCentreCmd(
    id: String,
    expectedVersion: Long,
    name: String,
    description: Option[String] = None)
      extends CentreCommand
      with HasIdentity
      with HasExpectedVersion

  case class EnableCentreCmd(
    id: String,
    expectedVersion: Long)
      extends CentreCommand
      with HasIdentity
      with HasExpectedVersion

  case class DisableCentreCmd(
    id: String,
    expectedVersion: Long)
      extends CentreCommand
      with HasIdentity
      with HasExpectedVersion

  case class AddCentreLocationCmd(
    centreId: String,
    name: String,
    street: String,
    city: String,
    province: String,
    postalCode: String,
    poBoxNumber: Option[String],
    countryIsoCode: String)
      extends CentreCommand
      with HasCentreIdentity

  case class RemoveCentreLocationCmd(
    centreId: String,
    locationId: String)
      extends CentreCommand
      with HasCentreIdentity

  case class AddCentreToStudyCmd(
    centreId: String,
    studyId: String)
      extends CentreCommand
      with HasCentreIdentity

  case class RemoveCentreFromStudyCmd(
    centreId: String,
    studyId: String)
      extends CentreCommand
      with HasCentreIdentity
}
