/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/*
 * AngularJS Factory
 */
/* @ngInject */
function CollectionSpecimenDescriptionFactory($log, DomainEntity, DomainError) {

  /*
   * Used for validation.
   */
  const SCHEMA = {
    'id': 'CollectionSpecimenDescription',
    'type': 'object',
    'properties': {
      'id':                          { 'type': 'string' },
      'slug':                        { 'type': 'string' },
      'name':                        { 'type': 'string' },
      'description':                 { 'type': [ 'string', 'null' ] },
      'units':                       { 'type': 'string' },
      'anatomicalSourceType':        { 'type': 'string' },
      'preservationType':            { 'type': 'string' },
      'preservationTemperature': { 'type': 'string' },
      'specimenType':                { 'type': 'string' },
      'maxCount':                    { 'type': 'integer', 'minimum': 1 },
      'amount':                      { 'type': 'number', 'minimum': 0 }
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
   * @class
   * @memberOf domain.studies
   *
   * @classdesc Used to configure a *Specimen Type* used by a {@link domain.studies.Study Study}.
   *
   * It records ownership, summary, storage, and classification information that applies to an entire group or
   * collection of {@link domain.participants.Specimen Specimens}. A *Specimen Description* is defined either for
   * specimen types collected from participants, or for specimen types that are processed.
   *
   * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
   * this class. Objects of this type are usually returned by the server's REST API.
   */
  class CollectionSpecimenDescription extends DomainEntity {

    constructor(obj) {
      /**
       * A short identifying name.
       * @name domain.studies.CollectionSpecimenDescription#name
       * @type string
       */

      /**
       * Specifies how the specimen amount is measured (e.g. volume, weight, length, etc.).
       * @name domain.studies.CollectionSpecimenDescription#units
       * @type {string}
       */

      /**
       * @name domain.studies.CollectionSpecimenDescription#anatomicalSourceType
       * @type {domain.AnatomicalSourceType}
       */

      /**
       * @name domain.studies.CollectionSpecimenDescription#preservationType
       * @type {domain.PreservationType}
       */

      /**
       * @name domain.studies.CollectionSpecimenDescription#preservationTemperature
       * @type {domain.PreservationTemperature}
       */

      /**
       * @name domain.studies.CollectionSpecimenDescription#specimenType
       * @type {domain.SpecimenType}
       */

      /**
       * The amount per specimen, measured in units, to be collected.
       * @name domain.studies.CollectionSpecimenDescription#amount
       * @type {number}
       * @see domain.studies.CollectionSpecimenDescription#units
       */

      /**
       * The number of specimens to be collected.
       * @name domain.studies.CollectionSpecimenDescription#maxCount
       * @type {number}
       * @see domain.studies.CollectionSpecimenDescription.units
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

    static create(obj) {
      var validation = CollectionSpecimenDescription.isValid(obj);

      if (!validation.valid) {
        $log.error('invalid object from server: ' + validation.message);
        throw new DomainError('invalid object from server: ' + validation.message);
      }
      return new CollectionSpecimenDescription(obj);
    }
  }

  return CollectionSpecimenDescription;
}

export default ngModule => ngModule.factory('CollectionSpecimenDescription',
                                           CollectionSpecimenDescriptionFactory)
