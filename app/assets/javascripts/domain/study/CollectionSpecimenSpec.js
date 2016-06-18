/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['lodash', 'tv4'], function(_, tv4) {
  'use strict';

  CollectionSpecimenSpecFactory.$inject = [
    'funutils',
    'validationService',
    'biobankApi'
  ];

  /**
   *
   */
  function CollectionSpecimenSpecFactory(funutils,
                                         validationService,
                                         biobankApi) {

    /**
     * Used for validation.
     */
    var schema = {
      'id': 'CollectionSpecimenSpec',
      'type': 'object',
      'properties': {
        'uniqueId':                    { 'type': 'string' },
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
        'uniqueId',
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
     * @classdesc Creates a new CollectionSpecimenSpec.
     * @class
     * @memberOf domain.studies
     *
     * @param {object} [obj={}] - An initialization object whose properties are the same as the members from
     * this class. Objects of this type are usually returned by the server's REST API.
     */
    function CollectionSpecimenSpec(obj) {
      /**
       * The unique ID that identifies an object of this type.
       * @name domain.studies.CollectionSpecimenSpec#uniqueId
       *
       * @type string
       */
      this.uniqueId = null;

      /**
       * A short identifying name.
       * @name domain.studies.CollectionSpecimenSpec#name
       * @type string
       */
      this.name = '';

      /**
       * A description that can provide additional details on the name.
       *
       * @name domain.studies.CollectionSpecimenSpec#description
       * @type {?string}
       * @see domain.studies.CollectionSpecimenSpec#name
       */
      this.description = null;

      /**
       * Specifies how the specimen amount is measured (e.g. volume, weight, length, etc.).
       * @name domain.studies.CollectionSpecimenSpec#units
       * @type {string}
       */
      this.units = '';

      /**
       * @name domain.studies.CollectionSpecimenSpec#anatomicalSourceType
       * @type {domain.AnatomicalSourceType}
       */
      this.anatomicalSourceType = '';

      /**
       * @name domain.studies.CollectionSpecimenSpec#preservationType
       * @type {domain.PreservationType}
       */
      this.preservationType = '';

      /**
       * @name domain.studies.CollectionSpecimenSpec#preservationTemperatureType
       * @type {domain.PreservationTemperatureType}
       */
      this.preservationTemperatureType = '';

      /**
       * @name domain.studies.CollectionSpecimenSpec#specimenType
       * @type {domain.SpecimenType}
       */
      this.specimenType = '';

      /**
       * The amount per specimen, measured in units, to be collected.
       * @name domain.studies.CollectionSpecimenSpec#amount
       * @type {number}
       * @see domain.studies.CollectionSpecimenSpec#units
       */
      this.units = null;

      /**
       * The number of specimens to be collected.
       * @name domain.studies.CollectionSpecimenSpec#maxCount
       * @type {number}
       * @see domain.studies.CollectionSpecimenSpec.units
       */
      this.maxCount = null;

      obj = obj || {};
      _.extend(this, obj);
    }

    /**
     * @private
     */
    CollectionSpecimenSpec.isValid = function(obj) {
      return tv4.validate(obj, schema);
    };

    return CollectionSpecimenSpec;
  }

  return CollectionSpecimenSpecFactory;

});
