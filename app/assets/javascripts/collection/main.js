/**
 * The Specimen collection module.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function(require) {
  'use strict';

  var angular                       = require('angular'),

      collectionDirective           = require('./directives/collection/collectionDirective'),
      selectStudyDirective          = require('./directives/selectStudy/selectStudyDirective'),

      participantAddDirective       = require('./directives/participantAdd/participantAddDirective'),
      participantGetDirective       = require('./directives/participantGet/participantGetDirective'),
      participantSummaryDirective   = require('./directives/participantSummary/participantSummaryDirective'),
      participantViewDirective      = require('./directives/participantView/participantViewDirective'),

      ceventAddDirective            = require('./directives/ceventAdd/ceventAddDirective'),
      ceventGetTypeDirective        = require('./directives/ceventGetType/ceventGetTypeDirective'),
      ceventViewDirective           = require('./directives/ceventView/ceventViewDirective'),
      ceventsAddAndSelect           = require('./directives/ceventsAddAndSelect/ceventsAddAndSelectDirective'),
      ceventsListDirective          = require('./directives/ceventsList/ceventsListDirective'),

      states                        = require('./states');

  var module = angular.module('biobank.collection', []);

  module.directive('collection',         collectionDirective);
  module.directive('selectStudy',        selectStudyDirective);

  module.directive('participantAdd',     participantAddDirective);
  module.directive('participantGet',     participantGetDirective);
  module.directive('participantSummary', participantSummaryDirective);
  module.directive('participantView',    participantViewDirective);

  module.directive('ceventAdd',           ceventAddDirective);
  module.directive('ceventGetType',       ceventGetTypeDirective);
  module.directive('ceventView',          ceventViewDirective);
  module.directive('ceventsAddAndSelect', ceventsAddAndSelect);
  module.directive('ceventsList',         ceventsListDirective);

  module.config(states);

  return module;
});
