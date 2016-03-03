/**
 * Study module.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var angular = require('angular'),
      name = 'biobank.admin.directives.studies',
      module,
      annotationTypes = require('./annotationTypes/main');

  module = angular.module(name, [
    annotationTypes.name
  ]);

  return {
    name: name,
    module: module
  };
});
