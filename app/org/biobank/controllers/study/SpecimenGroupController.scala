package org.biobank.controllers.study

import org.biobank.controllers._
import org.biobank.service._
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.service.{ ServiceComponent, TopComponentImpl }
import org.biobank.domain.study._
import org.biobank.domain.AnatomicalSourceType._
import org.biobank.domain.PreservationType._
import org.biobank.domain.PreservationTemperatureType._
import org.biobank.domain.SpecimenType._

import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps
import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import play.Logger
import play.api.libs.json._
import akka.util.Timeout
import securesocial.core.{ Identity, Authorization, SecureSocial }

import scalaz._
import scalaz.Scalaz._

/**
 * Handles all operations user can perform on a Specimen Group.
 */
object SpecimenGroupController extends Controller with SecureSocial {

  private val studyService = ApplicationComponent.studyService

  implicit val studyWrites = new Writes[SpecimenGroup] {

    def writes(sg: SpecimenGroup) = Json.obj(
      "studyId"                     -> sg.studyId.id,
      "id"                          -> sg.id.id,
      "version"                     -> sg.version,
      "addedDate"                   -> sg.addedDate,
      "lastUpdateDate"              -> sg.lastUpdateDate,
      "name"                        -> sg.name,
      "description"                 -> sg.description,
      "units"                       -> sg.units,
      "anatomicalSourceType"        -> sg.anatomicalSourceType.toString,
      "preservationType"            -> sg.preservationType.toString,
      "preservationTemperatureType" -> sg.preservationTemperatureType.toString,
      "specimenType"                -> sg.specimenType.toString
    )

  }

  implicit val anatomicalSourceTypeReads = EnumUtils.enumReads(org.biobank.domain.AnatomicalSourceType)
  implicit val preservationTypeReads = EnumUtils.enumReads(org.biobank.domain.PreservationType)
  implicit val preservationTemperatureTypeReads = EnumUtils.enumReads(org.biobank.domain.PreservationTemperatureType)
  implicit val specimenTypeReads = EnumUtils.enumReads(org.biobank.domain.SpecimenType)

  implicit val addSpecimenGroupCmdReads: Reads[AddSpecimenGroupCmd] = (
    (JsPath \ "studyId").read[String](minLength[String](2)) and
      (JsPath \ "name").read[String](minLength[String](2)) and
      (JsPath \ "description").readNullable[String](minLength[String](2)) and
      (JsPath \ "units").read[String](minLength[String](2)) and
      (JsPath \ "anatomicalSourceType").read[AnatomicalSourceType] and
      (JsPath \ "preservationType").read[PreservationType] and
      (JsPath \ "preservationTemperatureType").read[PreservationTemperatureType] and
      (JsPath \ "specimenType").read[SpecimenType]
  )(AddSpecimenGroupCmd.apply _)

  implicit val updateSpecimenGroupCmdReads: Reads[UpdateSpecimenGroupCmd] = (
    (JsPath \ "studyId").read[String](minLength[String](2)) and
      (JsPath \ "id").read[String](minLength[String](2)) and
      (JsPath \ "version").readNullable[Long](min[Long](0)) and
      (JsPath \ "name").read[String](minLength[String](2)) and
      (JsPath \ "description").readNullable[String](minLength[String](2)) and
      (JsPath \ "units").read[String](minLength[String](2)) and
      (JsPath \ "anatomicalSourceType").read[AnatomicalSourceType] and
      (JsPath \ "preservationType").read[PreservationType] and
      (JsPath \ "preservationTemperatureType").read[PreservationTemperatureType] and
      (JsPath \ "specimenType").read[SpecimenType]
  )(UpdateSpecimenGroupCmd.apply _)


}
