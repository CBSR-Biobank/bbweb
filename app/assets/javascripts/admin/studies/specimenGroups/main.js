/**
 * Study module.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var angular = require('angular'),
      name = 'biobank.admin.studies.specimenGroups',
      module;

  module = angular.module(name, [ 'biobank.users' ]);

  module.config(require('./states'));

  module.controller('SpecimenGroupEditCtrl', require('./SpecimenGroupEditCtrl'));
  module.service('specimenGroupUtils',       require('./specimenGroupUtilsService'));
  module.directive('specimenGroupsPanel',
                   require('./directives/specimenGroupsPanel/specimenGroupsPanelDirective'));

  return {
    name: name,
    module: module
  };
});
