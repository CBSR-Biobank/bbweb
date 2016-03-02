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

  module.service('annotationTypeAddService', require('./services/annotationTypeAddService'));

  module.directive(
    'annotationTypeAdd',
    require('./directives/annotationTypeAdd/annotationTypeAddDirective'));
  module.directive(
    'annotationTypeView',
    require('./directives/annotationTypeView/annotationTypeViewDirective'));
  module.directive(
    'annotationTypeSummary',
    require('./directives/annotationTypeSummary/annotationTypeSummaryDirective'));
  module.directive(
    'collectionEventAnnotationTypeAdd',
    require('./directives/collectionEventAnnotationTypeAdd/collectionEventAnnotationTypeAddDirective'));
  module.directive(
    'collectionEventAnnotationTypeView',
    require('./directives/collectionEventAnnotationTypeView/collectionEventAnnotationTypeViewDirective'));
  module.directive(
    'participantAnnotationTypeAdd',
    require('./directives/participantAnnotationTypeAdd/participantAnnotationTypeAddDirective'));
  module.directive(
    'participantAnnotationTypeView',
    require('./directives/participantAnnotationTypeView/participantAnnotationTypeViewDirective'));
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
