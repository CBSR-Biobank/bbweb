/**
 * Centre configuration module.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
*/
define(function (require) {
  'use strict';

  var angular = require('angular'),
      name = 'biobank.centres',
      module;

  module = angular.module(name, []);

  module.service('centreLocationsService', require('./centreLocationsService'));
  module.service('centresService',         require('./centresService'));

  return {
    name: name,
    module: module
  };
});
