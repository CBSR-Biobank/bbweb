package org.biobank.fixtures

import org.biobank.domain.Factory
import org.biobank.domain.studies._

trait ProcessingTypeFixtures {

  protected val factory: Factory

  private class SpecimenDefinitionFixtures {
    val study = factory.createDisabledStudy
    val collectionSpecimenDefinition = factory.createCollectionSpecimenDefinition
    val collectionEventType = factory.createCollectionEventType.copy(
        specimenDefinitions = Set(collectionSpecimenDefinition))

    val input = InputSpecimenProcessing(expectedChange       = BigDecimal(1.0),
                                        count                = 1,
                                        containerTypeId      = None,
                                        definitionType       = ProcessingType.collectedDefinition,
                                        entityId             = collectionEventType.id,
                                        specimenDefinitionId = collectionSpecimenDefinition.id)

    private val _processingType = factory.createProcessingType
    private val specimenProcessing = _processingType.specimenProcessing.copy(input = input)

    val processingType = _processingType.copy(specimenProcessing = specimenProcessing)
  }

  protected class CollectionSpecimenDefinitionFixtures {
    private val f = new SpecimenDefinitionFixtures
    val collectionSpecimenDefinition = f.collectionSpecimenDefinition
    val collectionEventType = f.collectionEventType
    val processingType = f.processingType
    val study = f.study
  }

  protected class ProcessedSpecimenDefinitionFixtures  {
    private val f = new SpecimenDefinitionFixtures
    val inputProcessingType = f.processingType
    val inputSpecimenDefinition = inputProcessingType.specimenProcessing.output.specimenDefinition
    val outputSpecimenDefinition = factory.createProcessedSpecimenDefinition
    val input = InputSpecimenProcessing(expectedChange       = BigDecimal(1.0),
                                        count                = 1,
                                        containerTypeId      = None,
                                        definitionType       = ProcessingType.processedDefinition,
                                        entityId             = inputProcessingType.id,
                                        specimenDefinitionId = inputSpecimenDefinition.id)

    private val _processingType = factory.createProcessingType
    private val specimenProcessing = _processingType.specimenProcessing.copy(input = input)

    val outputProcessingType = _processingType.copy(specimenProcessing = specimenProcessing)
    val study = f.study
  }

}
