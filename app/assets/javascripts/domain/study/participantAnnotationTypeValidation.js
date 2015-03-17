define(['underscore'], function(_) {
  'use strict';

  participantAnnotationTypeValidation.$inject = [
    'funutils',
    'validationService',
    'studyAnnotationTypeValidation'
  ];

  /**
   *
   */
  function participantAnnotationTypeValidation(funutils,
                                               validationService,
                                               studyAnnotationTypeValidation) {
    var service = {};

    var objRequiredKeys          = studyAnnotationTypeValidation.objRequiredKeys.concat('required');
    var addedEventRequiredKeys   = studyAnnotationTypeValidation.addedEventRequiredKeys.concat('required');
    var updatedEventRequiredKeys = studyAnnotationTypeValidation.updatedEventRequiredKeys.concat('required');

    var validateObj = validationService.condition1(
      validationService.validator('must be a map', _.isObject));

    var createObj = funutils.partial1(validateObj, _.identity);

    service.validateObj = funutils.partial1(
      validationService.condition1(
        validationService.validator('has the correct keys',
                                    validationService.hasKeys.apply(null, objRequiredKeys))),
      createObj);

    service.validateAddedEvent = funutils.partial1(
      validationService.condition1(
        validationService.validator('has the correct keys: [' + addedEventRequiredKeys.join(', ') + ']',
                                    validationService.hasKeys.apply(null, addedEventRequiredKeys))),
      createObj);

    service.validateUpdatedEvent = funutils.partial1(
      validationService.condition1(
        validationService.validator('has the correct keys',
                                    validationService.hasKeys.apply(null, updatedEventRequiredKeys))),
      createObj);

    return service;
  }

  return participantAnnotationTypeValidation;
});
