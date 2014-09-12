package org.biobank.infrastructure.command

import org.biobank.infrastructure._
import org.biobank.infrastructure.command.Commands._
import org.biobank.infrastructure.JsonUtils._

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

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
  implicit val addCentreCmdReads = (
    (__ \ "name").read[String](minLength[String](2)) and
      (__ \ "description").readNullable[String]
  )(AddCentreCmd.apply _ )

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
  )(AddCentreLocationCmd.apply _ )

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
