/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/* @ngInject */
function InputSpecimenProcessingFactory($log,
                                        biobankApi,
                                        SpecimenProcessingInfo,
                                        DomainEntity,
                                        DomainError) {

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

    /**
     * Creates an InputSpecimenProcessing object, but first it validates `obj` to ensure that it has a valid
     * schema.
     *
     * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
     * this class. Objects of this type are usually returned by the server's REST API.
     *
     * @returns {domain.studies.InputSpecimenProcessing} An object containing the input specimen information.
     */
    static create(obj) {
      var validation = InputSpecimenProcessing.isValid(obj);
      if (!validation.valid) {
        $log.error(validation.message);
        throw new DomainError(validation.message);
      }
      return new InputSpecimenProcessing(obj);
    }

  }

  return InputSpecimenProcessing;
}

export default ngModule => ngModule.factory('InputSpecimenProcessing', InputSpecimenProcessingFactory)
