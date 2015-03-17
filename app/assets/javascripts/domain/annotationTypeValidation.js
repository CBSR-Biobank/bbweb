define([], function() {
  'use strict';

  annotationTypeValidation.$inject = [
    'validationService'
  ];

  /**
   *
   */
  function annotationTypeValidation(validationService) {
    var requiredKeys = ['name', 'valueType', 'options'];
    var service = {};

    service.objRequiredKeys          = requiredKeys.concat('id');
    service.addedEventRequiredKeys   = requiredKeys.concat('annotationTypeId');
    service.updatedEventRequiredKeys = service.addedEventRequiredKeys.concat('version');

    return service;
  }

  return annotationTypeValidation;
});
