define(['underscore'], function(_) {
  'use strict';

  ProcessingDtoFactory.$inject = [
    'funutils',
    'validationService',
    'biobankApi',
    'ProcessingType',
    'SpecimenLinkType',
    'SpecimenLinkAnnotationType',
    'SpecimenGroup'
  ];

  function ProcessingDtoFactory(funutils,
                                validationService,
                                biobankApi,
                                ProcessingType,
                                SpecimenLinkType,
                                SpecimenLinkAnnotationType,
                                SpecimenGroup) {

    var requiredKeys = [
      'processingTypes',
      'specimenLinkTypes',
      'specimenLinkAnnotationTypes',
      'specimenGroups'
    ];

    var validateObj = funutils.partial(
      validationService.condition1(
        validationService.validator('must be a map', _.isObject),
        validationService.validator('has the correct keys',
                                    validationService.hasKeys.apply(null, requiredKeys))),
      _.identity);

    /**
     * An object that contains a list of all the processingTypes, specimenLinkTypes,
     * specimenLinkAnnotationTypes, and specimenGroups for a study.
     */
    function ProcessingDto(obj) {
      var self = this;

      obj = obj || {};

      obj.processingTypes = _.map(obj.processingTypes, function(obj) {
        return new ProcessingType(obj);
      });
      obj.specimenLinkTypes = _.map(obj.specimenLinkTypes, function(obj) {
        return new SpecimenLinkType(obj);
      });
      obj.specimenLinkAnnotationTypes = _.map(
        obj.specimenLinkAnnotationTypes,
        function (at) {
          return new SpecimenLinkAnnotationType(at);
        });
      obj.specimenGroups = _.map(obj.specimenGrops, function(obj) {
        return new SpecimenGroup(obj);
      });

      _.extend(self, _.defaults(obj, {
        processingTypes:             [],
        specimenLinkTypes:           [],
        specimenLinkAnnotationTypes: [],
        specimenGroups:              []
      }));
    }

    /**
     * Used by promise code, so it must return an error rather than throw one.
     */
    ProcessingDto.create = function (obj) {
      var validation = validateObj(obj);
      if (!_.isObject(validation)) {
        return new Error('invalid object from server: ' + validation);
      }
      return new ProcessingDto(obj);
    };

    ProcessingDto.get = function(studyId) {
      return biobankApi.get('/studies' + studyId + '/dto/processing')
        .then(function(reply) {
          return ProcessingDto.create(reply);
        });
    };

    return ProcessingDto;
  }

  return ProcessingDtoFactory;

});
