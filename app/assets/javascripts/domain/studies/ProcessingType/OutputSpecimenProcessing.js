/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/* @ngInject */
function OutputSpecimenProcessingFactory($log,
                                         biobankApi,
                                         SpecimenProcessingInfo,
                                         ProcessedSpecimenDefinition) {

  /*
   * Used for validating plain objects.
   */
  const SCHEMA = SpecimenProcessingInfo.createDerivedSchema({
    id: 'OutputSpecimenProcessing',
    properties: {
      'specimenDefinition': { '$ref': 'ProcessedSpecimenDefinition' }
    },
    required: [ 'specimenDefinition' ]
  });

  /**
   * @classdesc Defines the output specimen information for a {@link domain.studies.ProcessingType
   * ProcessingType}.
   *
   * @class
   * @memberOf domain.studies
   * @augments domain.studies.SpecimenProcessingInfo
   */
  class OutputSpecimenProcessing extends SpecimenProcessingInfo {

    /**
     * The expected amount to be added.
     *
     * @name domain.studies.OutputSpecimenProcessing#expectedChange
     * @type {number}
     */

    /**
     * The number of output specimens involved in processing.
     *
     * @name domain.studies.OutputSpecimenProcessing#count
     * @type {integer}
     */

    /**
     * The container type the output specimens are stored in.
     *
     * @name domain.studies.OutputSpecimenProcessing#containerTypeId
     * @type {string}
     * @default null
     */

    /**
     * The container type the output specimen definition.
     *
     * @name domain.studies.OutputSpecimenProcessing#specimenDefinition
     * @type {domain.studies.ProcessedSpecimenDefinition}
     */

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
      return [ ProcessedSpecimenDefinition.schema() ];
    }

  }

  return OutputSpecimenProcessing;
}

export default ngModule => ngModule.factory('OutputSpecimenProcessing', OutputSpecimenProcessingFactory)
