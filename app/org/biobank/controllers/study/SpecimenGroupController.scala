package org.biobank.controllers.study

import org.biobank.controllers._
import org.biobank.service._
import org.biobank.service.json.Study._
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.domain.study._
import org.biobank.domain.AnatomicalSourceType._
import org.biobank.domain.PreservationType._
import org.biobank.domain.PreservationTemperatureType._
import org.biobank.domain.SpecimenType._

import play.api.{ Play }
import play.api.mvc._
import play.api.libs.json._

import scalaz._
import scalaz.Scalaz._

/**
 * Handles all operations user can perform on a Specimen Group.
 */
object SpecimenGroupController extends BbwebController {

  private def studyService = Play.current.plugin[BbwebPlugin].map(_.studyService).getOrElse {
    sys.error("Bbweb plugin is not registered")
  }

  def list = Action {
    val json = Json.toJson(studyService.getAll.toList)
    Ok(json)
  }

}
