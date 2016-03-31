/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['underscore', 'tv4'], function(_, tv4) {
  'use strict';

  CollectionSpecimenSpecFactory.$inject = [
    'funutils',
    'validationService',
    'biobankApi',
    'ConcurrencySafeEntity'
  ];

  /**
   *
   */
  function CollectionSpecimenSpecFactory(funutils,
                                validationService,
                                biobankApi,
                                ConcurrencySafeEntity) {

    var schema = {
      'id': 'CollectionSpecimenSpec',
      'type': 'object',
      'properties': {
        'uniqueId':                    { 'type': 'string' },
        'name':                        { 'type': 'string' },
        'description':                 { 'type': 'string' },
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

    function CollectionSpecimenSpec(obj) {
      var self = this,
          defaults = {
            uniqueId:                    null,
            name:                        '',
            description:                 null,
            units:                       '',
            anatomicalSourceType:        '',
            preservationType:            '',
            preservationTemperatureType: '',
            specimenType:                '',
            maxCount:                    null,
            amount:                      null
          };

      obj = obj || {};
      ConcurrencySafeEntity.call(self, obj);
      _.extend(this, defaults, _.pick(obj, _.keys(defaults)));
    }

    CollectionSpecimenSpec.valid = function(obj) {
      return tv4.validate(obj, schema);
    };

    return CollectionSpecimenSpec;
  }

  return CollectionSpecimenSpecFactory;

});
