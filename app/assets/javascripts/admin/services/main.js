/**
 * Study module.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var angular = require('angular'),
      name = 'biobank.admin.services',
      module,
      studyAnnotationTypes = require('./studies/annotationTypes/main');

  module = angular.module(name, [
    studyAnnotationTypes.name
  ]);

return {
    name: name,
    module: module
  };
});
