/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
/**
 *
 */
/* @ngInject */
function CollectionSpecimenDescriptionFactory($log, DomainEntity, DomainError) {


  /**
   * @classdesc Creates a new CollectionSpecimenDescription.
   * @class
   * @memberOf domain.studies
   *
   * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
   * this class. Objects of this type are usually returned by the server's REST API.
   */
  function CollectionSpecimenDescription(obj) {
    /**
     * The ID that identifies an object of this type.
     * @name domain.studies.CollectionSpecimenDescription#id
     *
     * @type string
     */
    this.id = null;

    /**
     * A short identifying name.
     * @name domain.studies.CollectionSpecimenDescription#name
     * @type string
     */
    this.name = '';

    /**
     * A description that can provide additional details on the name.
     *
     * @name domain.studies.CollectionSpecimenDescription#description
     * @type {?string}
     * @see domain.studies.CollectionSpecimenDescription#name
     */
    this.description = null;

    /**
     * Specifies how the specimen amount is measured (e.g. volume, weight, length, etc.).
     * @name domain.studies.CollectionSpecimenDescription#units
     * @type {string}
     */
    this.units = '';

    /**
     * @name domain.studies.CollectionSpecimenDescription#anatomicalSourceType
     * @type {domain.AnatomicalSourceType}
     */
    this.anatomicalSourceType = '';

    /**
     * @name domain.studies.CollectionSpecimenDescription#preservationType
     * @type {domain.PreservationType}
     */
    this.preservationType = '';

    /**
     * @name domain.studies.CollectionSpecimenDescription#preservationTemperatureType
     * @type {domain.PreservationTemperatureType}
     */
    this.preservationTemperatureType = '';

    /**
     * @name domain.studies.CollectionSpecimenDescription#specimenType
     * @type {domain.SpecimenType}
     */
    this.specimenType = '';

    /**
     * The amount per specimen, measured in units, to be collected.
     * @name domain.studies.CollectionSpecimenDescription#amount
     * @type {number}
     * @see domain.studies.CollectionSpecimenDescription#units
     */
    this.units = null;

    /**
     * The number of specimens to be collected.
     * @name domain.studies.CollectionSpecimenDescription#maxCount
     * @type {number}
     * @see domain.studies.CollectionSpecimenDescription.units
     */
    this.maxCount = null;

    DomainEntity.call(this, CollectionSpecimenDescription.SCHEMA, obj);
  }

  CollectionSpecimenDescription.prototype = Object.create(DomainEntity.prototype);
  CollectionSpecimenDescription.prototype.constructor = CollectionSpecimenDescription;

  /**
   * Used for validation.
   */
  CollectionSpecimenDescription.SCHEMA = {
    'id': 'CollectionSpecimenDescription',
    'type': 'object',
    'properties': {
      'id':                          { 'type': 'string' },
      'name':                        { 'type': 'string' },
      'description':                 { 'type': [ 'string', 'null' ] },
      'units':                       { 'type': 'string' },
      'anatomicalSourceType':        { 'type': 'string' },
      'preservationType':            { 'type': 'string' },
      'preservationTemperatureType': { 'type': 'string' },
      'specimenType':                { 'type': 'string' },
      'maxCount':                    { 'type': 'integer', 'minimum': 1 },
      'amount':                      { 'type': 'number', 'minimum': 0 }
    },
    'required': [
      'id',
      'name',
      'units',
      'anatomicalSourceType',
      'preservationType',
      'preservationTemperatureType',
      'specimenType',
      'maxCount',
      'amount'
    ]
  };

  /**
   * @private
   */
  CollectionSpecimenDescription.isValid = function(obj) {
    return DomainEntity.isValid(CollectionSpecimenDescription.SCHEMA, null, obj);
  };

  CollectionSpecimenDescription.create = function(obj) {
    var validation = CollectionSpecimenDescription.isValid(obj);

    if (!validation.valid) {
      $log.error('invalid object from server: ' + validation.message);
      throw new DomainError('invalid object from server: ' + validation.message);
    }
    return new CollectionSpecimenDescription(obj);
  };

  return CollectionSpecimenDescription;
}

export default ngModule => ngModule.factory('CollectionSpecimenDescription',
                                           CollectionSpecimenDescriptionFactory)
