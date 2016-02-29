/**
 * Study module.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var angular = require('angular'),
      name = 'biobank.admin.studies.annotationTypes',
      module;

  module = angular.module(name, ['biobank.users']);

  module.service('studyAnnotationTypeUtils',  require('./studyAnnotationTypeUtilsService'));

  module.directive(
    'annotationTypeAdd',
    require('./directives/annotationTypeAdd/annotationTypeAddDirective'));
  module.directive(
    'annotationTypeView',
    require('./directives/annotationTypeView/annotationTypeViewDirective'));
  module.directive(
    'participantAnnotationTypeAdd',
    require('./directives/participantAnnotationTypeAdd/participantAnnotationTypeAddDirective'));
  module.directive(
    'studyAnnotationTypesPanel',
    require('./directives/studyAnnotationTypesPanel/studyAnnotationTypesPanelDirective'));
  module.directive(
    'studyAnnotationTypesTable',
    require('./directives/studyAnnotationTypesTable/studyAnnotationTypesTableDirective'));

  return {
    name: name,
    module: module
  };
});
