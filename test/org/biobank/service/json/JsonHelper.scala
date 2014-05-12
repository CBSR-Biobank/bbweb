package org.biobank.service.json

import org.biobank.domain.study._

import play.api.libs.json._
import org.scalatest.Assertions._
import org.joda.time.format.ISODateTimeFormat

object JsonHelper {

  val fmt = ISODateTimeFormat.dateTime();

  def compareObj(json: JsValue, study: Study)  = {
    assert((json \ "id").as[String]                     === study.id.id)
    assert((json \ "version").as[Long]                  === study.version)
    assert((json \ "addedDate").as[String]              === fmt.print(study.addedDate))
    assert((json \ "lastUpdateDate").as[Option[String]] === study.lastUpdateDate.map(fmt.print(_)))
    assert((json \ "name").as[String]                   === study.name)
    assert((json \ "description").as[Option[String]]    === study.description)
    assert((json \ "status").as[String]                 === study.status)
  }

  def compareObj(json: JsValue, specimenGroup: SpecimenGroup)  = {
    assert((json \ "studyId").as[String]                     === specimenGroup.studyId.id)
    assert((json \ "id").as[String]                          === specimenGroup.id.id)
    assert((json \ "version").as[Long]                       === specimenGroup.version)
    assert((json \ "addedDate").as[String]                   === fmt.print(specimenGroup.addedDate))
    assert((json \ "lastUpdateDate").as[Option[String]]      === specimenGroup.lastUpdateDate.map(fmt.print(_)))
    assert((json \ "name").as[String]                        === specimenGroup.name)
    assert((json \ "description").as[Option[String]]         === specimenGroup.description)
    assert((json \ "units").as[String]                       === specimenGroup.units)
    assert((json \ "anatomicalSourceType").as[String]        === specimenGroup.anatomicalSourceType.toString)
    assert((json \ "preservationType").as[String]            === specimenGroup.preservationType.toString)
    assert((json \ "preservationTemperatureType").as[String] === specimenGroup.preservationTemperatureType.toString)
    assert((json \ "specimenType").as[String]                === specimenGroup.specimenType.toString)
  }

}
