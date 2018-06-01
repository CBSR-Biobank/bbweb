/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/* @ngInject */
function SpecimenProcessingInfoFactory($log,
                                       biobankApi,
                                       DomainEntity,
                                       DomainError) {

  /*
   * Used for validating plain objects.
   */
  const SCHEMA = {
    'id': 'SpecimenProcessingInfo',
    'type': 'object',
    'properties': {
      'expectedChange':  { 'type': 'number', 'minimum':  0 },
      'count':           { 'type': 'integer', 'minimum':  1 },
      'containerTypeId': { 'type': [ 'string', 'null' ] }
    },
    'required': [ 'expectedChange', 'count', 'containerTypeId' ]
  };


  /**
   * @classdesc Base class used to define how specimens are processed.
   *
   * @class
   * @abstratct
   * @memberOf domain.studies
   */
  class SpecimenProcessingInfo extends DomainEntity {

    /**
     * The expected amount to be removed / added.
     *
     * @name domain.studies.SpecimenProcessingInfo#expectedChange
     * @type {number}
     */

    /**
     * The number of input / output specimens involved in processing.
     *
     * @name domain.studies.SpecimenProcessingInfo#count
     * @type {integer}
     */

    /**
     * The container type the input / output specimens are stored in.
     *
     * @name domain.studies.SpecimenProcessingInfo#containerTypeId
     * @type {string}
     * @default null
     */

    /**
     * @protected
     */
    static create(obj) { // eslint-disable-line no-unused-vars
      throw new DomainError('should be done by derived class');
    }

    /**
     * Used to create a JSON schema for a derived class.
     *
     * @protected
     */
    static createDerivedSchema({ id,
                                 type = 'object',
                                 properties = {},
                                 required = [] } = {}) {
      return Object.assign(
        {},
        SCHEMA,
        {
          'id': id,
          'type': type,
          'properties': Object.assign(
            {},
            SCHEMA.properties,
            properties
          ),
          'required': SCHEMA.required.slice().concat(required)
        }
      );
    }
  }

  return SpecimenProcessingInfo;
}

export default ngModule => ngModule.factory('SpecimenProcessingInfo', SpecimenProcessingInfoFactory)
