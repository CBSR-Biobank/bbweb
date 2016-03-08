/**
 * Study module.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var angular = require('angular'),
      name = 'biobank.admin.directives.studies.ceventTypes',
      module;

  module = angular.module(name, [
    'biobank.users'
  ]);

  module.directive('ceventTypeAdd',
                   require('./ceventTypeAdd/ceventTypeAddDirective'));
  module.directive('ceventTypesAddAndSelect',
                   require('./ceventTypesAddAndSelect/ceventTypesAddAndSelectDirective'));
  module.directive('ceventTypeView',
                   require('./ceventTypeView/ceventTypeViewDirective'));
  module.directive('collectionSpecimenSpecAdd',
                   require('./collectionSpecimenSpecAdd/collectionSpecimenSpecAddDirective'));
  module.directive('collectionSpecimenSpecSummary',
                   require('./collectionSpecimenSpecSummary/collectionSpecimenSpecSummaryDirective'));
  module.directive('collectionSpecimenSpecView',
                   require('./collectionSpecimenSpecView/collectionSpecimenSpecViewDirective'));

  return {
    name: name,
    module: module
  };
});
