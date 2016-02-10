/**
 * Study module.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
*/
define(function (require) {
  'use strict';

  var angular = require('angular'),
      name = 'biobank.admin.users',
      module;

  module = angular.module(name, [ 'biobank.studies' ]);

  module.config(require('./states'));
  module.controller('UsersTableCtrl', require('./UsersTableCtrl'));

  return {
    name: name,
    module: module
  };
});
