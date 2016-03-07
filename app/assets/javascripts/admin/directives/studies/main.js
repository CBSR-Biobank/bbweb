/**
 * Study module.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var angular = require('angular'),
      name = 'biobank.admin.directives.studies',
      module,
      annotationTypes = require('./annotationTypes/main'),
      collection = require('./collection/main'),
      processing = require('./processing/main');

  module = angular.module(name, [
    annotationTypes.name,
    collection.name,
    processing.name
  ]);

  module.directive('studiesList',             require('./studiesList/studiesListDirective'));
  module.directive('studyAdd',                require('./studyAdd/studyAddDirective'));
  module.directive('studyCollection',         require('./studyCollection/studyCollectionDirective'));
  module.directive('studyParticipantsTab',    require('./studyParticipantsTab/studyParticipantsTabDirective'));
  module.directive('studySummary',            require('./studySummary/studySummaryDirective'));
  module.directive('studyView',               require('./studyView/studyViewDirective'));
  module.directive('studyNotDisabledWarning',
                   require('./studyNotDisabledWarning/studyNotDisabledWarningDirective'));
  module.directive('validAmount',          require('./validAmount/validAmountDirective'));
  module.directive('validCount',           require('./validCount/validCountDirective'));

  return {
    name: name,
    module: module
  };
});
