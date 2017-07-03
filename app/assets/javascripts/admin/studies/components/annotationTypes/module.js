/**
 * Study module.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var angular = require('angular'),
      name = 'admin.studies.components.annotationTypes',
      module;

  module = angular.module(name, ['biobank.users']);

  module
    .component('collectionEventAnnotationTypeAdd',
               require('./collectionEventAnnotationTypeAdd/collectionEventAnnotationTypeAddComponent'))
    .component('collectionEventAnnotationTypeView',
               require('./collectionEventAnnotationTypeView/collectionEventAnnotationTypeViewComponent'))
    .component('participantAnnotationTypeAdd',
               require('./participantAnnotationTypeAdd/participantAnnotationTypeAddComponent'))
    .component('participantAnnotationTypeView',
               require('./participantAnnotationTypeView/participantAnnotationTypeViewComponent'));

  return {
    name: name,
    module: module
  };
});
