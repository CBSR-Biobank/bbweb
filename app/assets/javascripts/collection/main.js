/**
 * The Specimen collection module.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var angular = require('angular'),
      name = 'biobank.collection',
      module;

  module = angular
    .module(name, [ 'biobank.users' ])

    .config(require('./states'))

    .service('specimenAddModal',      require('./services/specimenAddModal/specimenAddModalService'))

    .directive('collection',          require('./directives/collection/collectionDirective'))
    .directive('selectStudy',         require('./directives/selectStudy/selectStudyDirective'))

    .directive('participantAdd',      require('./directives/participantAdd/participantAddDirective'))
    .directive('participantGet',      require('./directives/participantGet/participantGetDirective'))
    .directive('participantSummary',
               require('./directives/participantSummary/participantSummaryDirective'))

    .directive('participantView',     require('./directives/participantView/participantViewDirective'))

    .directive('ceventAdd',           require('./directives/ceventAdd/ceventAddDirective'))
    .directive('ceventGetType',       require('./directives/ceventGetType/ceventGetTypeDirective'))
    .directive('ceventView',          require('./directives/ceventView/ceventViewDirective'))
    .directive('ceventsAddAndSelect',
               require('./directives/ceventsAddAndSelect/ceventsAddAndSelectDirective'))
    .directive('ceventsList',         require('./directives/ceventsList/ceventsListDirective'))

    .component('ceventSpecimensView',
               require('./components/ceventSpecimensView/ceventSpecimensViewComponent'));

  return {
    name: name,
    module: module
  };
});
