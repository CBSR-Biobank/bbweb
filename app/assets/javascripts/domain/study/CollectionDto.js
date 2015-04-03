define(['underscore'], function(_) {
  'use strict';

  CollectionDtoFactory.$inject = [
    'funutils',
    'validationService',
    'biobankApi',
    'CollectionType',
    'CollectionEventType',
    'CollectionEventAnnotationType',
    'SpecimenGroup'
  ];

  function CollectionDtoFactory(funutils,
                                validationService,
                                biobankApi,
                                CollectionType,
                                CollectionEventType,
                                CollectionEventAnnotationType,
                                SpecimenGroup) {

    var requiredKeys = [
      'collectionEventTypes',
      'collectionEventAnnotationTypes',
      'collectionEventAnnotationTypesInUse',
      'specimenGroups'
    ];

    var validateObj = funutils.partial(
      validationService.condition1(
        validationService.validator('must be a map', _.isObject),
        validationService.validator('has the correct keys',
                                    validationService.hasKeys.apply(null, requiredKeys))),
      _.identity);

    /**
     * An object that contains a list of all the collectionEventTypes, collectionEventAnnotationTypes, and
     * specimenGroups for a study.
     */
    // FIXME: convert collectionDto.specimenGroups to SpecimenGroup objects
    function CollectionDto(obj) {
      var self = this;

      obj = obj || {};

      obj.collectionEventTypes = _.map(obj.collectionEventTypes, function(obj) {
        return new CollectionEventType(obj);
      });
      obj.collectionEventAnnotationTypes = _.map(
        obj.collectionEventAnnotationTypes,
        function (at) {
          return new CollectionEventAnnotationType(at);
        });
      obj.specimenGroups = _.map(obj.specimenGrops, function(obj) {
        return new SpecimenGroup(obj);
      });

      _.extend(self, _.defaults(obj, {
        collectionTypes:                [],
        collectionEventTypes:           [],
        collectionEventAnnotationTypes: [],
        specimenGroups:                 []
      }));
    }

    /**
     * Used by promise code, so it must return an error rather than throw one.
     */
    CollectionDto.create = function (obj) {
      var validation = validateObj(obj);
      if (!_.isObject(validation)) {
        return new Error('invalid object from server: ' + validation);
      }
      return new CollectionDto(obj);
    };

    CollectionDto.get = function(studyId) {
      return biobankApi.get('/studies' + studyId + '/dto/collection')
        .then(function(reply) {
          return CollectionDto.create(reply);
        });
    };

    return CollectionDto;
  }

  return CollectionDtoFactory;

});
