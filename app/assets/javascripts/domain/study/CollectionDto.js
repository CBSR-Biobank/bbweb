/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['underscore'], function(_) {
  'use strict';

  CollectionDtoFactory.$inject = [
    'funutils',
    'validationService',
    'biobankApi',
    'CollectionEventType',
    'CollectionEventAnnotationType',
    'SpecimenGroup'
  ];

  function CollectionDtoFactory(funutils,
                                validationService,
                                biobankApi,
                                CollectionEventType,
                                CollectionEventAnnotationType,
                                SpecimenGroup) {

    var requiredKeys = [
      'collectionEventTypes',
      'collectionEventAnnotationTypes',
      'collectionEventAnnotationTypeIdsInUse',
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
    function CollectionDto(obj) {
      var self = this,
          defaults = {
            collectionEventAnnotationTypes:        [],
            specimenGroups:                        [],
            collectionEventTypes:                  [],
            collectionEventAnnotationTypeIdsInUse: []
          };

      obj = obj || {};
      _.extend(this, defaults, _.pick(obj, 'collectionEventAnnotationTypeIdsInUse'));

      self.collectionEventAnnotationTypes = _.map(
        obj.collectionEventAnnotationTypes,
        function (serverAnnotationType) {
          return new CollectionEventAnnotationType(serverAnnotationType);
        });

      self.specimenGroups = _.map(obj.specimenGroups, function(serverSpecimenGroup) {
        return new SpecimenGroup(serverSpecimenGroup);
      });

      self.collectionEventTypes = _.map(obj.collectionEventTypes, function(serverCet) {
        var cet = new CollectionEventType(serverCet);
        cet.studySpecimenGroups(self.specimenGroups);
        cet.studyAnnotationTypes(self.collectionEventAnnotationTypes);
        return cet;
      });
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
      return biobankApi.get('/studies/' + studyId + '/dto/collection')
        .then(function(reply) {
          return CollectionDto.create(reply);
        });
    };

    return CollectionDto;
  }

  return CollectionDtoFactory;

});
