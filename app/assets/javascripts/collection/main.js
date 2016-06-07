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
      specimenAddModule = require('./specimenAdd/specimenAddModule'),
      module;

  module = angular.module(name, [
    'biobank.users',
    specimenAddModule.name
  ]);

  module.config(require('./states'));

  module.directive('collection',          require('./directives/collection/collectionDirective'));
  module.directive('selectStudy',         require('./directives/selectStudy/selectStudyDirective'));

  module.directive('participantAdd',      require('./directives/participantAdd/participantAddDirective'));
  module.directive('participantGet',      require('./directives/participantGet/participantGetDirective'));
  module.directive('participantSummary',
                   require('./directives/participantSummary/participantSummaryDirective'));
  module.directive('participantView',     require('./directives/participantView/participantViewDirective'));

  module.directive('ceventAdd',           require('./directives/ceventAdd/ceventAddDirective'));
  module.directive('ceventGetType',       require('./directives/ceventGetType/ceventGetTypeDirective'));
  module.directive('ceventView',          require('./directives/ceventView/ceventViewDirective'));
  module.directive('ceventsAddAndSelect',
                   require('./directives/ceventsAddAndSelect/ceventsAddAndSelectDirective'));
  module.directive('ceventsList',         require('./directives/ceventsList/ceventsListDirective'));

  module.component('ceventSpecimensView',     require('./components/ceventSpecimensView/ceventSpecimensViewComponent'));

  return {
    name: name,
    module: module
  };
});
