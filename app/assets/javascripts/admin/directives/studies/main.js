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
      ceventTypes = require('./ceventTypes/main'),
      processing = require('./processing/main');

  module = angular.module(name, [
    annotationTypes.name,
    ceventTypes.name
  ]);

  module.directive('studiesList',             require('./studiesList/studiesListDirective'));
  module.directive('studyCollection',         require('./studyCollection/studyCollectionDirective'));
  module.directive('studyNotDisabledWarning',
                   require('./studyNotDisabledWarning/studyNotDisabledWarningDirective'));
  module.directive('validAmount',          require('./validAmount/validAmountDirective'));
  module.directive('validCount',           require('./validCount/validCountDirective'));

  return {
    name: name,
    module: module
  };
});
