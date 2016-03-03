/**
 * Study module.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var angular = require('angular'),
      name = 'biobank.admin.studies',
      module,
      ceventTypes = require('./ceventTypes/main'),
      participants = require('./participants/main'),
      processing = require('./processing/main'),
      specimenGroups = require('./specimenGroups/main');

  module = angular.module(name, [
    ceventTypes.name,
    participants.name,
    processing.name,
    specimenGroups.name,
    'biobank.users'
  ]);

  module.config(require('./states'));

  module.directive('studiesList',             require('./directives/studiesList/studiesListDirective'));
  module.directive('studyCollection',         require('./directives/studyCollection/studyCollectionDirective'));
  module.directive('studyNotDisabledWarning',
                   require('./directives/studyNotDisabledWarning/studyNotDisabledWarningDirective'));

  module.controller('StudyCtrl',           require('./StudyCtrl'));
  module.controller('StudyEditCtrl',       require('./StudyEditCtrl'));
  module.controller('StudySummaryTabCtrl', require('./StudySummaryTabCtrl'));
  module.directive('validAmount',          require('./directives/validAmount/validAmountDirective'));
  module.directive('validCount',           require('./directives/validCount/validCountDirective'));

  return {
    name: name,
    module: module
  };
});
