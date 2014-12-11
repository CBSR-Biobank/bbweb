package org.biobank.infrastructure.command

import org.biobank.infrastructure._
import org.biobank.infrastructure.command.Commands._
import org.biobank.infrastructure.JsonUtils._

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

object CentreCommands {

  trait CentreCommand extends Command

  trait CentreModifyCommand extends CentreCommand with HasIdentity with HasExpectedVersion

  trait CentreCommandWithCentreId extends CentreCommand with HasCentreIdentity

  case class AddCentreCmd(
    name: String,
    description: Option[String] = None)
      extends CentreCommand

  case class UpdateCentreCmd(
    id: String,
    expectedVersion: Long,
    name: String,
    description: Option[String] = None)
      extends CentreModifyCommand

  case class EnableCentreCmd(
    id: String,
    expectedVersion: Long)
      extends CentreModifyCommand

  case class DisableCentreCmd(
    id: String,
    expectedVersion: Long)
      extends CentreModifyCommand

  // centre location commands

  trait CentreLocationCmd extends CentreCommandWithCentreId

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

  case class RemoveCentreLocationCmd(
    centreId: String,
    locationId: String)
      extends CentreLocationCmd

  trait CentreStudyCmd extends CentreCommandWithCentreId

  case class AddStudyToCentreCmd(centreId: String, studyId: String) extends CentreStudyCmd

  case class RemoveStudyFromCentreCmd(centreId: String, studyId: String) extends CentreStudyCmd

  implicit val addCentreCmdReads: Reads[AddCentreCmd] = (
    (__ \ "name").read[String](minLength[String](2)) and
      (__ \ "description").readNullable[String]
  )(AddCentreCmd.apply _)

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

  implicit val addCentreLocationCmdReads: Reads[AddCentreLocationCmd] = (
    (__ \ "centreId").read[String](minLength[String](2)) and
      (__ \ "name").read[String](minLength[String](2)) and
      (__ \ "street").read[String] and
      (__ \ "city").read[String] and
      (__ \ "province").read[String] and
      (__ \ "postalCode").read[String] and
      (__ \ "poBoxNumber").readNullable[String] and
      (__ \ "countryIsoCode").read[String]
  )(AddCentreLocationCmd.apply _)

  implicit val removeCentreLocationCmdReads: Reads[RemoveCentreLocationCmd] = (
    (__ \ "centreId").read[String](minLength[String](2)) and
      (__ \ "locationId").read[String](minLength[String](2))
  )(RemoveCentreLocationCmd.apply _ )

  implicit val addStudyToCentreCmdReads: Reads[AddStudyToCentreCmd] = (
    (__ \ "centreId").read[String](minLength[String](2)) and
      (__ \ "studyId").read[String](minLength[String](2))
  )(AddStudyToCentreCmd.apply _ )

  implicit val removeStudyFromCentreCmdReads: Reads[RemoveStudyFromCentreCmd] = (
    (__ \ "centreId").read[String](minLength[String](2)) and
      (__ \ "studyId").read[String](minLength[String](2))
  )(RemoveStudyFromCentreCmd.apply _ )

}
