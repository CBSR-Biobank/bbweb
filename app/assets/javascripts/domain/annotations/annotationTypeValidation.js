/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/**
 * @deprecated
 */
function annotationTypeValidation() {
  var requiredKeys = ['name', 'valueType', 'options'];
  var service = {};

  service.objRequiredKeys          = requiredKeys.concat('id');
  service.addedEventRequiredKeys   = requiredKeys.concat('annotationTypeId');
  service.updatedEventRequiredKeys = service.addedEventRequiredKeys.concat('version');

  return service;
}

export default ngModule => ngModule.service('annotationTypeValidation', annotationTypeValidation)
