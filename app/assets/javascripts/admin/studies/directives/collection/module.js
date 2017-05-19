/**
 * Study module.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var angular = require('angular'),
      name = 'admin.studies.directives.collection',
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
  module.directive('collectionSpecimenDescriptionAdd',
                   require('./collectionSpecimenDescriptionAdd/collectionSpecimenDescriptionAddDirective'));
  module.directive('collectionSpecimenDescriptionSummary',
                   require('./collectionSpecimenDescriptionSummary/collectionSpecimenDescriptionSummaryDirective'));
  module.directive('collectionSpecimenDescriptionView',
                   require('./collectionSpecimenDescriptionView/collectionSpecimenDescriptionViewDirective'));

  return {
    name: name,
    module: module
  };
});
