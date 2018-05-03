package org.biobank.fixtures

import org.biobank.domain.Factory
import org.biobank.domain.studies._
import scala.language.reflectiveCalls

trait ProcessingTypeFixtures {

  protected val factory: Factory

  protected def collectedSpecimenDerivationFixtures() = {
    new {
      val collectedSpecimenDefinition = factory.createCollectionSpecimenDefinition
      val collectionEventType = factory.createCollectionEventType.copy(
          specimenDefinitions = Set(collectedSpecimenDefinition))
      val processingSpecimenDefinition = factory.createProcessingSpecimenDefinition
      val specimenDerivation = CollectedSpecimenDerivation(collectionEventType.id,
                                                           collectedSpecimenDefinition.id,
                                                           processingSpecimenDefinition)
      val processingType = factory.createProcessingType.copy(specimenDerivation = specimenDerivation)
      val study = factory.defaultDisabledStudy
    }
  }

  protected def processedSpecimenDerivationFixtures() = {
    val f = collectedSpecimenDerivationFixtures
    new {
      val inputProcessingType = f.processingType
      val inputSpecimenDefinition = f.processingSpecimenDefinition
      val outputSpecimenDefinition = factory.createProcessingSpecimenDefinition
      val specimenDerivation = ProcessedSpecimenDerivation(inputProcessingType.id,
                                                           inputSpecimenDefinition.id,
                                                           outputSpecimenDefinition)
      val outputProcessingType = factory.createProcessingType.copy(specimenDerivation = specimenDerivation)
      val study = factory.defaultDisabledStudy
    }
  }

}
