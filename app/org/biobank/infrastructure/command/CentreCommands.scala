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

  case class AddCentreCmd(userId:      Option[String],
                          name:        String,
                          description: Option[String] = None)
      extends CentreCommand

  case class UpdateCentreCmd(userId:          Option[String],
                             id:              String,
                             expectedVersion: Long,
                             name:            String,
                             description:     Option[String] = None)
      extends CentreModifyCommand

  case class EnableCentreCmd(userId:          Option[String],
                             id:              String,
                             expectedVersion: Long)
      extends CentreModifyCommand

  case class DisableCentreCmd(userId:          Option[String],
                              id:              String,
                              expectedVersion: Long)
      extends CentreModifyCommand

  // centre location commands

  trait CentreLocationCmd extends CentreCommandWithCentreId

  case class AddCentreLocationCmd(userId:         Option[String],
                                  centreId:       String,
                                  name:           String,
                                  street:         String,
                                  city:           String,
                                  province:       String,
                                  postalCode:     String,
                                  poBoxNumber:    Option[String],
                                  countryIsoCode: String)
      extends CentreLocationCmd

  case class RemoveCentreLocationCmd(userId:     Option[String],
                                     centreId:   String,
                                     locationId: String)
      extends CentreLocationCmd

  trait CentreStudyCmd extends CentreCommandWithCentreId

  case class AddStudyToCentreCmd(userId:   Option[String],
                                 centreId: String,
                                 studyId:  String) extends CentreStudyCmd

  case class RemoveStudyFromCentreCmd(userId:   Option[String],
                                      centreId: String,
                                      studyId:  String) extends CentreStudyCmd

  implicit val addCentreCmdReads             = Json.reads[AddCentreCmd]
  implicit val updateCentreCmdReads          = Json.reads[UpdateCentreCmd]
  implicit val enableCentreCmdReads          = Json.reads[EnableCentreCmd]
  implicit val disableCentreCmdReads         = Json.reads[DisableCentreCmd]
  implicit val addCentreLocationCmdReads     = Json.reads[AddCentreLocationCmd]
  implicit val removeCentreLocationCmdReads  = Json.reads[RemoveCentreLocationCmd]
  implicit val addStudyToCentreCmdReads      = Json.reads[AddStudyToCentreCmd]
  implicit val removeStudyFromCentreCmdReads = Json.reads[RemoveStudyFromCentreCmd]

}
