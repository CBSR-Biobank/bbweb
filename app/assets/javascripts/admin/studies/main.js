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

  module
    .component('studiesPagedList',        require('./components/studiesPagedList/studiesPagedListComponent'))
    .component('ceventTypeView',          require('./components/ceventTypeView/ceventTypeViewComponent'))
    .component('ceventTypesAddAndSelect',
               require('./components/ceventTypesAddAndSelect/ceventTypesAddAndSelectComponent'))
    .component('studyCollection',         require('./components/studyCollection/studyCollectionComponent'))
    .component('studiesAdmin',            require('./components/studiesAdmin/studiesAdminComponent'))
    .component('studyAdd',                require('./components/studyAdd/studyAddComponent'))

    .directive('studyParticipantsTab',    require('./directives/studyParticipantsTab/studyParticipantsTabDirective'))
    .directive('studySummary',            require('./directives/studySummary/studySummaryDirective'))
    .directive('studyView',               require('./directives/studyView/studyViewDirective'))
    .directive('studyNotDisabledWarning',
               require('./directives/studyNotDisabledWarning/studyNotDisabledWarningDirective'))

    .config(require('./states'))
    .config(require('./ceventTypes/states'))
    .config(require('./participants/states'));

  return {
    name: name,
    module: module
  };
});
