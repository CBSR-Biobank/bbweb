/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash'

/**
 *
 */
/* @ngInject */
function studyAnnotationTypeValidation(funutils,
                                       validationService,
                                       annotationTypeValidation) {
  var service = {
    objRequiredKeys:          annotationTypeValidation.objRequiredKeys.concat('studyId'),
    addedEventRequiredKeys:   annotationTypeValidation.addedEventRequiredKeys.concat('studyId'),
    updatedEventRequiredKeys: annotationTypeValidation.updatedEventRequiredKeys.concat('studyId')
  };

  var validateObj = validationService.condition1(
    validationService.validator('must be a map', _.isObject));

  var createObj = funutils.partial1(validateObj, _.identity);

  service.validateObj = funutils.partial1(
    validationService.condition1(
      validationService.validator('has the correct keys',
                                  validationService.hasKeys.apply(null,
                                                                  service.objRequiredKeys))),
    createObj);

  service.validateAddedEvent = funutils.partial1(
    validationService.condition1(
      validationService.validator('has the correct keys: [' + service.addedEventRequiredKeys.join(', ') + ']',
                                  validationService.hasKeys.apply(null,
                                                                  service.addedEventRequiredKeys))),
    createObj);

  service.validateUpdatedEvent = funutils.partial1(
    validationService.condition1(
      validationService.validator('has the correct keys',
                                  validationService.hasKeys.apply(null,
                                                                  service.updatedEventRequiredKeys))),
    createObj);


  return service;
}

export default ngModule => ngModule.service('studyAnnotationTypeValidation', studyAnnotationTypeValidation)
