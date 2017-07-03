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

  module = angular.module(name, [ 'biobank.users' ]);

  module
    .directive('collectionSpecimenDescriptionAdd',
               require('./collectionSpecimenDescriptionAdd/collectionSpecimenDescriptionAddDirective'))
    .directive('collectionSpecimenDescriptionSummary',
               require('./collectionSpecimenDescriptionSummary/collectionSpecimenDescriptionSummaryDirective'))
    .directive('collectionSpecimenDescriptionView',
               require('./collectionSpecimenDescriptionView/collectionSpecimenDescriptionViewDirective'));

  return {
    name: name,
    module: module
  };
});
