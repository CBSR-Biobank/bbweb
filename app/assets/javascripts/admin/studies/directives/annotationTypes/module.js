/**
 * Study module.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var angular = require('angular'),
      name = 'admin.studies.directives.annotationTypes',
      module;

  module = angular.module(name, ['biobank.users']);

  module.directive('collectionEventAnnotationTypeAdd',
                   require('./collectionEventAnnotationTypeAdd/collectionEventAnnotationTypeAddDirective'));
  module.directive('collectionEventAnnotationTypeView',
                   require('./collectionEventAnnotationTypeView/collectionEventAnnotationTypeViewDirective'));
  module.directive('participantAnnotationTypeAdd',
                   require('./participantAnnotationTypeAdd/participantAnnotationTypeAddDirective'));
  module.directive('participantAnnotationTypeView',
                   require('./participantAnnotationTypeView/participantAnnotationTypeViewDirective'));

  return {
    name: name,
    module: module
  };
});
