package org.biobank.domain.participants

import org.biobank.domain.Factory
import scala.language.reflectiveCalls
import org.biobank.services.centres.CentreLocationInfo

trait SpecimenSpecFixtures {

  protected val factory: Factory

  protected def createEntities() = {
    val _centre = factory.createEnabledCentre.copy(locations = Set(factory.createLocation))
    val _study = factory.createEnabledStudy
    val _specimenDefinition = factory.createCollectionSpecimenDefinition
    val _ceventType = factory.createCollectionEventType.copy(studyId = _study.id,
                                                             specimenDefinitions = Set(_specimenDefinition),
                                                             annotationTypes = Set.empty)
    val _participant = factory.createParticipant.copy(studyId = _study.id)
    val _cevent = factory.createCollectionEvent

    val _centreLocationInfo =
      CentreLocationInfo(_centre.id.id,
                         _centre.locations.head.id.id,
                         _centre.name,
                         _centre.locations.head.name)

    new {
      val centre              = _centre
      val centreLocationInfo  = _centreLocationInfo
      val study               = _study
      val specimenDefinition = _specimenDefinition
      val ceventType          = _ceventType
      val participant         = _participant
      val cevent              = _cevent
    }
  }

  protected def createEntitiesAndSpecimens() = {
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
    }
  }
}
