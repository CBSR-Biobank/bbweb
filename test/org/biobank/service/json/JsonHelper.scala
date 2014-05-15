package org.biobank.service.json

import org.biobank.domain.study._

import org.scalatest.Matchers
import play.api.libs.json._
import org.scalatest.Assertions._
import com.github.nscala_time.time.Imports._

object JsonHelper extends Matchers {
  import org.biobank.service.json.JsonUtils._

  def compareObj(json: JsValue, study: Study)  = {
    (json \ "id").as[String]                     should be (study.id.id)
    (json \ "version").as[Long]                  should be (study.version)
    (json \ "name").as[String]                   should be (study.name)
    (json \ "description").as[Option[String]]    should be (study.description)
    (json \ "status").as[String]                 should be (study.status)

    ((json \ "addedDate").as[DateTime] to study.addedDate).millis should be < 1000L
    (json \ "lastUpdateDate").as[Option[DateTime]] map { dateTime =>
      (dateTime to study.lastUpdateDate.get).millis should be < 1000L
    }
  }

  def compareObj(json: JsValue, specimenGroup: SpecimenGroup)  = {
    (json \ "studyId").as[String]                     should be (specimenGroup.studyId.id)
    (json \ "id").as[String]                          should be (specimenGroup.id.id)
    (json \ "version").as[Long]                       should be (specimenGroup.version)
    (json \ "name").as[String]                        should be (specimenGroup.name)
    (json \ "description").as[Option[String]]         should be (specimenGroup.description)
    (json \ "units").as[String]                       should be (specimenGroup.units)
    (json \ "anatomicalSourceType").as[String]        should be (specimenGroup.anatomicalSourceType.toString)
    (json \ "preservationType").as[String]            should be (specimenGroup.preservationType.toString)
    (json \ "preservationTemperatureType").as[String] should be (specimenGroup.preservationTemperatureType.toString)
    (json \ "specimenType").as[String]                should be (specimenGroup.specimenType.toString)

    ((json \ "addedDate").as[DateTime] to specimenGroup.addedDate).millis should be < 1000L
    (json \ "lastUpdateDate").as[Option[DateTime]] map { dateTime =>
      (dateTime to specimenGroup.lastUpdateDate.get).millis should be < 1000L
    }
  }

}
