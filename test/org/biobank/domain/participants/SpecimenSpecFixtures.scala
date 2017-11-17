package org.biobank.domain.participants

import org.biobank.domain.Factory
import org.biobank.domain.study.CollectionSpecimenDescription
import scala.language.reflectiveCalls
import org.biobank.service.centres.CentreLocationInfo

trait SpecimenSpecFixtures {

  val factory: Factory

  def createEntities() = {
    val _centre = factory.createEnabledCentre.copy(locations = Set(factory.createLocation))
    val _study = factory.createEnabledStudy
    val _specimenDescription = factory.createCollectionSpecimenDescription
    val _ceventType = factory.createCollectionEventType.copy(studyId = _study.id,
                                                             specimenDescriptions = Set(_specimenDescription),
                                                             annotationTypes = Set.empty)
    val _participant = factory.createParticipant.copy(studyId = _study.id)
    val _cevent = factory.createCollectionEvent

    val _centreLocationInfo =
      CentreLocationInfo(_centre.id.id,
                         _centre.locations.head.id.id,
                         _centre.name,
                         _centre.locations.head.name)

    new {
      val centre             = _centre
      val centreLocationInfo = _centreLocationInfo
      val study              = _study
      val specimenDescription       = _specimenDescription
      val ceventType         = _ceventType
      val participant        = _participant
      val cevent             = _cevent
    }
  }

  def createEntitiesAndSpecimens() = {
    val entities = createEntities

    val _specimens = (1 to 2).map { _ => factory.createUsableSpecimen }.toList

    new {
      val centre             = entities.centre
      val centreLocationInfo = entities.centreLocationInfo
      val study              = entities.study
      val participant        = entities.participant
      val ceventType         = entities.ceventType
      val cevent             = entities.cevent
      val specimens          = _specimens
      val specimenDtos       = specimensToDtos(_specimens,
                                               entities.cevent,
                                               entities.specimenDescription,
                                               entities.centreLocationInfo,
                                               entities.centreLocationInfo)
    }
  }

  def specimensToDtos(specimens:              List[Specimen],
                      cevent:                 CollectionEvent,
                      specimenDescription:           CollectionSpecimenDescription,
                      fromCentreLocationInfo: CentreLocationInfo,
                      toCentreLocationInfo:   CentreLocationInfo) =
    specimens.map  { s =>
      s.createDto(cevent, specimenDescription, fromCentreLocationInfo, toCentreLocationInfo)
    }
}
