/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/* @ngInject */
function InputSpecimenProcessingFactory($log,
                                        biobankApi,
                                        SpecimenProcessingInfo) {

  /*
   * Used for validating plain objects.
   */
  const SCHEMA = SpecimenProcessingInfo.createDerivedSchema({
    id: 'InputSpecimenProcessing',
    properties: {
      'definitionType':       { 'type': 'string' },
      'entityId':             { 'type': 'string' },
      'specimenDefinitionId': { 'type': 'string' }
    },
    required: [ 'definitionType',  'entityId','specimenDefinitionId' ]
  });

  /**
   * @classdesc Defines the input specimen information for a {@link domain.studies.ProcessingType
   * ProcessingType}.
   *
   * @class
   * @memberOf domain.studies
   * @augments domain.studies.SpecimenProcessingInfo
   */
  class InputSpecimenProcessing extends SpecimenProcessingInfo {


    constructor(obj = {}) {
      /**
       * The expected amount to be removed.
       *
       * @name domain.studies.SpecimenProcessingInfo#expectedChange
       * @type {number}
       */

      /**
       * The number of input specimens involved in processing.
       *
       * @name domain.studies.SpecimenProcessingInfo#count
       * @type {integer}
       */

      /**
       * The container type the input specimens are stored in.
       *
       * @name domain.studies.SpecimenProcessingInfo#containerTypeId
       * @type {string}
       * @default null
       */

      /**
       * Whether the input specimen is a collected or processed specimen.
       *
       * @name domain.studies.InputSpecimenProcessing#definitionType
       * @type {string}
       */

      /**
       * The ID of the entity that defines the specimen definition.
       *
       * @name domain.studies.InputSpecimenProcessing#entityId
       * @type {string}
       */

      /**
       * The container type the input specimens are stored in.
       *
       * @name domain.studies.InputSpecimenProcessing#specimenDefinitionId
       * @type {string}
       */

      super(Object.assign(
        {
          expectedChange:       undefined,
          count:                undefined,
          containerTypeId:      null,
          definitionType:       undefined,
          entityId:             undefined,
          specimenDefinitionId: undefined
        },
        obj
      ));
    }

    /**
     * @private
     * @return {object} The JSON schema for this class.
     */
    static schema() {
      return SCHEMA;
    }

    /**
     * @private
     */
    static additionalSchemas() {
      return [];
    }

    setDefinitionType(isCollected) {
      this.definitionType = isCollected ? 'collected' : 'processed';
    }

    isCollected() {
      return (this.definitionType && this.definitionType === 'collected');
    }

    isProcessed() {
      return (this.definitionType && this.definitionType === 'processed');
    }

  }

  return InputSpecimenProcessing;
}

export default ngModule => ngModule.factory('InputSpecimenProcessing', InputSpecimenProcessingFactory)
