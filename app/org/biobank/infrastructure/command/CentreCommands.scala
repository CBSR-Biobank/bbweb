package org.biobank.infrastructure.command

import org.biobank.infrastructure.command.Commands._

import play.api.libs.json._
import play.api.libs.json.Reads._

object CentreCommands {

  trait CentreCommand extends Command with HasUserId

  trait CentreModifyCommand extends CentreCommand with HasIdentity with HasExpectedVersion

  trait CentreCommandWithCentreId extends CentreCommand with HasCentreIdentity

  final case class AddCentreCmd(userId:      String,
                                name:        String,
                                description: Option[String])
      extends CentreCommand

  final case class UpdateCentreNameCmd(userId:          String,
                                       id:              String,
                                       expectedVersion: Long,
                                       name:            String)
      extends CentreModifyCommand

  final case class UpdateCentreDescriptionCmd(userId:          String,
                                              id:              String,
                                              expectedVersion: Long,
                                              description:     Option[String])
      extends CentreModifyCommand

  final case class EnableCentreCmd(userId:          String,
                                   id:              String,
                                   expectedVersion: Long)
      extends CentreModifyCommand

  final case class DisableCentreCmd(userId:          String,
                                    id:              String,
                                    expectedVersion: Long)
      extends CentreModifyCommand

  final case class AddCentreLocationCmd(userId:          String,
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

  final case class UpdateCentreLocationCmd(userId:          String,
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

  final case class RemoveCentreLocationCmd(userId:          String,
                                           id:              String,
                                           expectedVersion: Long,
                                           locationId:      String)
      extends CentreModifyCommand

  trait CentreStudyCmd extends CentreCommandWithCentreId

  final case class AddStudyToCentreCmd(userId:          String,
                                       id:              String,
                                       expectedVersion: Long,
                                       studyId:         String)
      extends CentreModifyCommand

  final case class RemoveStudyFromCentreCmd(userId:          String,
                                            id:              String,
                                            expectedVersion: Long,
                                            studyId:         String)
      extends CentreModifyCommand

  implicit val addCentreCmdReads: Reads[AddCentreCmd]                             = Json.reads[AddCentreCmd]
  implicit val updateCentreNameCmdReads: Reads[UpdateCentreNameCmd]               = Json.reads[UpdateCentreNameCmd]
  implicit val updateCentreDescriptionCmdReads: Reads[UpdateCentreDescriptionCmd] = Json.reads[UpdateCentreDescriptionCmd]
  implicit val enableCentreCmdReads: Reads[EnableCentreCmd]                       = Json.reads[EnableCentreCmd]
  implicit val disableCentreCmdReads: Reads[DisableCentreCmd]                     = Json.reads[DisableCentreCmd]
  implicit val addCentreLocationCmdReads: Reads[AddCentreLocationCmd]             = Json.reads[AddCentreLocationCmd]
  implicit val updateCentreLocationCmdReads: Reads[UpdateCentreLocationCmd]       = Json.reads[UpdateCentreLocationCmd]
  implicit val removeCentreLocationCmdReads: Reads[RemoveCentreLocationCmd]       = Json.reads[RemoveCentreLocationCmd]
  implicit val addStudyToCentreCmdReads: Reads[AddStudyToCentreCmd]               = Json.reads[AddStudyToCentreCmd]
  implicit val removeStudyFromCentreCmdReads: Reads[RemoveStudyFromCentreCmd]     = Json.reads[RemoveStudyFromCentreCmd]

}
