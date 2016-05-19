package org.biobank.infrastructure.command

import org.biobank.infrastructure.command.Commands._

import play.api.libs.json._
import play.api.libs.json.Reads._

object CentreCommands {

  trait CentreCommand extends Command with HasUserId

  trait CentreModifyCommand extends CentreCommand with HasIdentity with HasExpectedVersion

  trait CentreCommandWithCentreId extends CentreCommand with HasCentreIdentity

  case class AddCentreCmd(userId:      String,
                          name:        String,
                          description: Option[String] = None)
      extends CentreCommand

  case class UpdateCentreNameCmd(userId:          String,
                                 id:              String,
                                 expectedVersion: Long,
                                 name:            String)
      extends CentreModifyCommand

  case class UpdateCentreDescriptionCmd(userId:          String,
                                        id:              String,
                                        expectedVersion: Long,
                                        description:     Option[String])
      extends CentreModifyCommand

  case class EnableCentreCmd(userId:          String,
                             id:              String,
                             expectedVersion: Long)
      extends CentreModifyCommand

  case class DisableCentreCmd(userId:          String,
                              id:              String,
                              expectedVersion: Long)
      extends CentreModifyCommand

  case class AddCentreLocationCmd(userId:          String,
                                  id:              String,
                                  expectedVersion: Long,
                                  name:            String,
                                  street:          String,
                                  city:            String,
                                  province:        String,
                                  postalCode:      String,
                                  poBoxNumber:     Option[String],
                                  countryIsoCode:  String)
      extends CentreModifyCommand

  case class UpdateCentreLocationCmd(userId:          String,
                                     id:              String,
                                     locationId:      String,
                                     expectedVersion: Long,
                                     name:            String,
                                     street:          String,
                                     city:            String,
                                     province:        String,
                                     postalCode:      String,
                                     poBoxNumber:     Option[String],
                                     countryIsoCode:  String)
      extends CentreModifyCommand

  case class RemoveCentreLocationCmd(userId:          String,
                                     id:              String,
                                     expectedVersion: Long,
                                     locationId:      String)
      extends CentreModifyCommand

  trait CentreStudyCmd extends CentreCommandWithCentreId

  case class AddStudyToCentreCmd(userId:          String,
                                 id:              String,
                                 expectedVersion: Long,
                                 studyId:         String)
      extends CentreModifyCommand

  case class RemoveStudyFromCentreCmd(userId:          String,
                                      id:              String,
                                      expectedVersion: Long,
                                      studyId:         String)
      extends CentreModifyCommand

  implicit val addCentreCmdReads               = Json.reads[AddCentreCmd]
  implicit val updateCentreNameCmdReads        = Json.reads[UpdateCentreNameCmd]
  implicit val updateCentreDescriptionCmdReads = Json.reads[UpdateCentreDescriptionCmd]
  implicit val enableCentreCmdReads            = Json.reads[EnableCentreCmd]
  implicit val disableCentreCmdReads           = Json.reads[DisableCentreCmd]
  implicit val addCentreLocationCmdReads       = Json.reads[AddCentreLocationCmd]
  implicit val updateCentreLocationCmdReads    = Json.reads[UpdateCentreLocationCmd]
  implicit val removeCentreLocationCmdReads    = Json.reads[RemoveCentreLocationCmd]
  implicit val addStudyToCentreCmdReads        = Json.reads[AddStudyToCentreCmd]
  implicit val removeStudyFromCentreCmdReads   = Json.reads[RemoveStudyFromCentreCmd]

}
