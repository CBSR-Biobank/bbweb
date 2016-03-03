/**
 * Study module.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var angular = require('angular'),
      name = 'biobank.admin.studies.annotationTypes',
      module;

  module = angular.module(name, ['biobank.users']);

  module.service('studyAnnotationTypeUtils',  require('./studyAnnotationTypeUtilsService'));

  module.service('annotationTypeAddService', require('./services/annotationTypeAddService'));

return {
    name: name,
    module: module
  };
});
