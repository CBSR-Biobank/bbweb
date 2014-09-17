package org.biobank.infrastructure.command

import org.biobank.infrastructure._
import org.biobank.infrastructure.command.Commands._
import org.biobank.infrastructure.JsonUtils._

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

object CentreCommands {

  trait CentreCommand extends Command {
    val id: String
    val expectedVersion: Long
  }

  trait HasCentreIdentity { val centreId: String }

  case class AddCentreCmd(
    id: String,               // don't care
    expectedVersion: Long,    // don't care
    name: String,
    description: Option[String] = None)
      extends CentreCommand

  object AddCentreCmd {
    def apply(name: String, description: Option[String]): AddCentreCmd =
      AddCentreCmd("", -1, name, description)
  }

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

  // centre location commands

  trait CentreLocationCmd extends Command {
    val centreId: String
  }

  case class AddCentreLocationCmd(
    centreId: String,
    name: String,
    street: String,
    city: String,
    province: String,
    postalCode: String,
    poBoxNumber: Option[String],
    countryIsoCode: String)
      extends CentreLocationCmd
      with HasCentreIdentity

  case class RemoveCentreLocationCmd(
    centreId: String,
    locationId: String)
      extends CentreLocationCmd
      with HasCentreIdentity

  case class AddCentreToStudyCmd(
    centreId: String,
    studyId: String)
      extends CentreLocationCmd
      with HasCentreIdentity

  case class RemoveCentreFromStudyCmd(
    centreId: String,
    studyId: String)
      extends CentreLocationCmd
      with HasCentreIdentity
  implicit val addCentreCmdReads = (
    (__ \ "name").read[String](minLength[String](2)) and
      (__ \ "description").readNullable[String]
  )(AddCentreCmd("", -1, _, _))

  implicit val updateCentreCmdReads: Reads[UpdateCentreCmd] = (
      (__ \ "id").read[String](minLength[String](2)) and
      (__ \ "expectedVersion").read[Long](min[Long](0)) and
      (__ \ "name").read[String](minLength[String](2)) and
      (__ \ "description").readNullable[String]
  )(UpdateCentreCmd.apply _)

  implicit val enableCentreCmdReads: Reads[EnableCentreCmd] = (
    (__ \ "id").read[String](minLength[String](2)) and
      (__ \ "expectedVersion").read[Long](min[Long](0))
  )(EnableCentreCmd.apply _ )

  implicit val disableCentreCmdReads: Reads[DisableCentreCmd] = (
    (__ \ "id").read[String](minLength[String](2)) and
      (__ \ "expectedVersion").read[Long](min[Long](0))
  )(DisableCentreCmd.apply _)

  implicit val addCentreLocationCmdReads = (
    (__ \ "centreId").read[String](minLength[String](2)) and
      (__ \ "name").read[String](minLength[String](2)) and
      (__ \ "street").read[String](minLength[String](2)) and
      (__ \ "city").read[String](minLength[String](2)) and
      (__ \ "province").read[String](minLength[String](2)) and
      (__ \ "postalCode").read[String](minLength[String](2)) and
      (__ \ "poBoxNumber").readNullable[String](minLength[String](2)) and
      (__ \ "countryIsoCode").read[String](minLength[String](2))
  )(AddCentreLocationCmd(_, _, _, _, _, _, _, _))

  implicit val removeCentreLocationCmdReads = (
    (__ \ "centreId").read[String](minLength[String](2)) and
      (__ \ "locationId").read[String](minLength[String](2))
  )(RemoveCentreLocationCmd.apply _ )

  implicit val addCentreToStudyCmdReads = (
    (__ \ "centreId").read[String](minLength[String](2)) and
      (__ \ "studyId").read[String](minLength[String](2))
  )(AddCentreToStudyCmd.apply _ )

  implicit val removeCentreFromStudyCmdReads = (
    (__ \ "centreId").read[String](minLength[String](2)) and
      (__ \ "studyId").read[String](minLength[String](2))
  )(RemoveCentreFromStudyCmd.apply _ )

}
