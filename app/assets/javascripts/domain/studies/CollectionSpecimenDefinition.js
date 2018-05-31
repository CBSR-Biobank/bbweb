/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/*
 * AngularJS Factory
 */
/* @ngInject */
function CollectionSpecimenDefinitionFactory($log, DomainEntity) {

  /*
   * Used for validation.
   */
  const SCHEMA = {
    'id': 'CollectionSpecimenDefinition',
    'type': 'object',
    'properties': {
      'id':                      { 'type': 'string' },
      'slug':                    { 'type': 'string' },
      'name':                    { 'type': 'string' },
      'description':             { 'type': [ 'string', 'null' ] },
      'units':                   { 'type': 'string' },
      'anatomicalSourceType':    { 'type': 'string' },
      'preservationType':        { 'type': 'string' },
      'preservationTemperature': { 'type': 'string' },
      'specimenType':            { 'type': 'string' },
      'maxCount':                { 'type': 'integer', 'minimum': 1 },
      'amount':                  { 'type': 'number', 'minimum':  0 }

    },
    'required': [
      'id',
      'slug',
      'name',
      'units',
      'anatomicalSourceType',
      'preservationType',
      'preservationTemperature',
      'specimenType',
      'maxCount',
      'amount'
    ]
  }

  /**
   * Used to configure a *Specimen Type* used by a {@link domain.studies.Study Study}.
   *
   * It records ownership, summary, storage, and classification information that applies to an entire group or
   * collection of {@link domain.participants.Specimen Specimens}. A *Collection Specimen Definition* is
   * defined for specimen types collected from participants.
   *
   * @memberOf domain.studies
   */
  class CollectionSpecimenDefinition extends DomainEntity {

    /**
     * @param {object} obj - An initialization object whose properties are the same as the members from
     * this class. Objects of this type are usually returned by the server's REST API.
     */
    constructor(obj) {
      /**
       * A short identifying name.
       * @name domain.studies.CollectionSpecimenDefinition#name
       * @type string
       */

      /**
       * Specifies how the specimen amount is measured (e.g. volume, weight, length, etc.).
       * @name domain.studies.CollectionSpecimenDefinition#units
       * @type {string}
       */

      /**
       * @name domain.studies.CollectionSpecimenDefinition#anatomicalSourceType
       * @type {domain.AnatomicalSourceType.AnatomicalSourceType}
       */

      /**
       * @name domain.studies.CollectionSpecimenDefinition#preservationType
       * @type {domain.PreservationType.PreservationType}
       */

      /**
       * @name domain.studies.CollectionSpecimenDefinition#preservationTemperature
       * @type {domain.PreservationTemperature.PreservationTemperature}
       */

      /**
       * @name domain.studies.CollectionSpecimenDefinition#specimenType
       * @type {domain.studies.SpecimenType.SpecimenType}
       */

      /**
       * The amount per specimen, measured in units, to be collected.
       * @name domain.studies.CollectionSpecimenDefinition#amount
       * @type {number}
       * @see #units
       */

      /**
       * The number of specimens to be collected.
       * @name domain.studies.CollectionSpecimenDefinition#maxCount
       * @type {number}
       * @see #units
       */

      super(Object.assign(
        {
          name: '',
          description: null,
          anatomicalSourceType: '',
          preservationType: '',
          preservationTemperature: '',
          specimenType: '',
          units: null,
          maxCount: null
        },
        obj));
    }

    /** @private */
    static schema() {
      return SCHEMA;
    }

    /** @private */
    static additionalSchemas() {
      return [];
    }

  }

  return CollectionSpecimenDefinition;
}

export default ngModule => ngModule.factory('CollectionSpecimenDefinition',
                                           CollectionSpecimenDefinitionFactory)
