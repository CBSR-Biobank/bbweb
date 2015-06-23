/**
 * Centre configuration module.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
*/
define([
  'angular',
  './centresService',
  './centreLocationsService'
], function(angular,
            centresService,
            centreLocationsService) {
  'use strict';

  var module = angular.module('biobank.centres', []);

  module.service('centreLocationsService', centreLocationsService);
  module.service('centresService', centresService);

  return module;
});
