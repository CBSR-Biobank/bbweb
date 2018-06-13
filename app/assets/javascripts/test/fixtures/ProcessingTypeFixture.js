import _ from 'lodash';

export default class ProcessingTypeFixture {

  constructor(Factory,
              Study,
              CollectionEventType,
              ProcessingType) {
    Object.assign(this,
                  {
                    Factory,
                    Study,
                    CollectionEventType,
                    ProcessingType
                  });
  }

  /**
   * @returns {object} a plain processing type object.
   */
  fromEventType(eventType) {
    expect(eventType.specimenDefinitions).toBeNonEmptyArray();
    const inputSpecimenProcessing = this.Factory.inputSpecimenProcessing({
      definitionType:       'collected',
      entityId:             eventType.id,
      specimenDefinitionId: eventType.specimenDefinitions[0].id
    });
    const plainPtAnnotationType = this.Factory.annotationType();
    return this.Factory.processingType({
      specimenProcessing : {
        input: inputSpecimenProcessing
      },
      annotationTypes: [ plainPtAnnotationType ]
    });
  }

  /**
   * @returns {object} a plain processing type object.
   */
  fromProcessingType(inputProcessingType) {
    const inputSpecimenProcessing = this.Factory.inputSpecimenProcessing({
      definitionType:       'processed',
      entityId:             inputProcessingType.id,
      specimenDefinitionId: inputProcessingType.specimenProcessing.output.specimenDefinition.id
    });
    const plainPtAnnotationType = this.Factory.annotationType();
    return this.Factory.processingType({
      specimenProcessing : {
        input: inputSpecimenProcessing
      },
      annotationTypes: [ plainPtAnnotationType ]
    });
  }

  fixture({
    numEventTypes = numEventTypes,
    numProcessingTypesFromCollected: numProcessingTypesFromCollected,
    numProcessingTypesFromProcessed: numProcessingTypesFromProcessed
  } = {
    numEventTypes: 1,
    numProcessingTypesFromCollected: 1,
    numProcessingTypesFromProcessed: 1
  }) {
    const plainStudy = this.Factory.study();

    const eventTypes = _.range(numEventTypes).map(() => {
      const plainEventType = this.Factory.collectionEventType({
        specimenDefinitions: [ this.Factory.collectionSpecimenDefinition() ]
      });

      return {
        plainEventType,
        eventType: this.CollectionEventType.create(plainEventType)
      };
    });

    const processingTypesFromCollected =
          _.range(numProcessingTypesFromCollected).map((index) => {
            const eventType = eventTypes[index].plainEventType;
            const plainProcessingType = this.fromEventType(eventType);

            return {
              plainProcessingType,
              processingType: this.ProcessingType.create(plainProcessingType)
            };
          });

    const processingTypesFromProcessed =
          _.range(numProcessingTypesFromProcessed).map((index) => {
            const inputProcessingType = processingTypesFromCollected[index].plainProcessingType;
            const plainProcessingType =
                  this.fromProcessingType(inputProcessingType);

            return {
              plainProcessingType,
              processingType: this.ProcessingType.create(plainProcessingType)
            };
          });

    const existingEventTypes = eventTypes.map(et => et.plainEventType);
    const collectionSpecimenDefinitionNames =
          this.Factory.collectionSpecimenDefinitionNames(...existingEventTypes);

    const existingProcessingTypes =
          processingTypesFromCollected.map(ptfc => ptfc.plainProcessingType)
          .concat(processingTypesFromProcessed.map(ptfc => ptfc.plainProcessingType));
    const processedSpecimenDefinitionNames =
          this.Factory.processedSpecimenDefinitionNames(...existingProcessingTypes);

    return {
      plainStudy,
      study: this.Study.create(plainStudy),
      eventTypes,
      processingTypesFromCollected,
      processingTypesFromProcessed,
      collectionSpecimenDefinitionNames,
      processedSpecimenDefinitionNames
    };
  }


}
