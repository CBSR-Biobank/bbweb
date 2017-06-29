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

    .component('ceventSpecimensView',
               require('./components/ceventSpecimensView/ceventSpecimensViewComponent'))

    .component('specimenView',        require('./components/specimenView/specimenViewComponent'))
    .component('collection',          require('./components/collection/collectionComponent'))
    .component('ceventAdd',           require('./components/ceventAdd/ceventAddComponent'))
    .component('ceventView',          require('./components/ceventView/ceventViewComponent'))
    .component('ceventsList',         require('./components/ceventsList/ceventsListComponent'))

    .service('specimenAddModal',      require('./services/specimenAddModal/specimenAddModalService'))

    .directive('selectStudy',         require('./directives/selectStudy/selectStudyDirective'))

    .directive('participantAdd',      require('./directives/participantAdd/participantAddDirective'))
    .directive('participantGet',      require('./directives/participantGet/participantGetDirective'))
    .directive('participantSummary',
               require('./directives/participantSummary/participantSummaryDirective'))

    .directive('participantView',     require('./directives/participantView/participantViewDirective'))

    .directive('ceventGetType',       require('./directives/ceventGetType/ceventGetTypeDirective'))
    .directive('ceventsAddAndSelect',
               require('./directives/ceventsAddAndSelect/ceventsAddAndSelectDirective'));

  return {
    name: name,
    module: module
  };
});
