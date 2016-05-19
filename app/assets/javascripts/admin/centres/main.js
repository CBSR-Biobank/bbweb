/**
 * Study module.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
*/
define(function (require) {
  'use strict';

  var angular = require('angular'),
      name = 'biobank.admin.centres',
      module;

  module = angular.module(name, ['biobank.users']);

  module.config(require('./states'));

  return {
    name: name,
    module: module
  };
});
