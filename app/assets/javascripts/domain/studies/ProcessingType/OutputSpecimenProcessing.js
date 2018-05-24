/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/* @ngInject */
function OutputSpecimenProcessingFactory($log,
                                         biobankApi,
                                         SpecimenProcessingInfo,
                                         ProcessedSpecimenDefinition,
                                         DomainEntity,
                                         DomainError) {

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

    /**
     * Creates an OutputSpecimenProcessing object, but first it validates `obj` to ensure that it has a valid
     * schema.
     *
     * @private
     *
     * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
     * this class. Objects of this type are usually returned by the server's REST API.
     *
     * @returns {domain.studies.OutputSpecimenProcessing} An object containing the output specimen information.
     */
    static create(obj) {
      var validation = OutputSpecimenProcessing.isValid(obj);
      if (!validation.valid) {
        $log.error(validation.message);
        throw new DomainError(validation.message);
      }
      return new OutputSpecimenProcessing(obj);
    }

  }

  return OutputSpecimenProcessing;
}

export default ngModule => ngModule.factory('OutputSpecimenProcessing', OutputSpecimenProcessingFactory)
