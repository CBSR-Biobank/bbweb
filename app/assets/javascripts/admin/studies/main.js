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
      annotationTypesDirectives = require('./directives/annotationTypes/module'),
      collectionDirectives = require('./directives/collection/module'),
      processingDirecitves = require('./directives/processing/module'),
      processing = require('./processing/main'),
      specimenGroups = require('./specimenGroups/main');

  module = angular.module(name, [
    annotationTypesDirectives.name,
    collectionDirectives.name,
    processingDirecitves.name,
    processing.name,
    specimenGroups.name,
    'biobank.users'
  ]);

  module.directive('studiesList',             require('./directives/studiesList/studiesListDirective'));
  module.directive('studyAdd',                require('./directives/studyAdd/studyAddDirective'));
  module.directive('studyCollection',         require('./directives/studyCollection/studyCollectionDirective'));
  module.directive('studyParticipantsTab',    require('./directives/studyParticipantsTab/studyParticipantsTabDirective'));
  module.directive('studySummary',            require('./directives/studySummary/studySummaryDirective'));
  module.directive('studyView',               require('./directives/studyView/studyViewDirective'));
  module.directive('studyNotDisabledWarning',
                   require('./directives/studyNotDisabledWarning/studyNotDisabledWarningDirective'));

  module.config(require('./states'));
  module.config(require('./ceventTypes/states'));
  module.config(require('./participants/states'));

  return {
    name: name,
    module: module
  };
});
