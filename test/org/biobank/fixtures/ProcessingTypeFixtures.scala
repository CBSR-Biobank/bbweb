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
                                        entityId             = collectionEventType.id.id,
                                        specimenDefinitionId = collectionSpecimenDefinition.id)

    val processingType = factory.createProcessingType.copy(input = input)
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
    val inputProcessingType = f.processingType.copy(inUse = true)
    val inputSpecimenDefinition = inputProcessingType.output.specimenDefinition
    val outputSpecimenDefinition = factory.createProcessedSpecimenDefinition
    val input = InputSpecimenProcessing(expectedChange       = BigDecimal(1.0),
                                        count                = 1,
                                        containerTypeId      = None,
                                        definitionType       = ProcessingType.processedDefinition,
                                        entityId             = inputProcessingType.id.id,
                                        specimenDefinitionId = inputSpecimenDefinition.id)

    private val _processingType = factory.createProcessingType.copy(input = input)
    val outputProcessingType = _processingType
    val study = f.study
  }

}
