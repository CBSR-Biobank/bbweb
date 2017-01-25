/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var angular = require('angular'),
  name     = 'biobank.admin',
  module,
  centres  = require('biobank.admin.centres'),
  studies  = require('biobank.admin.studies'),
  users    = require('biobank.admin.users');

  module = angular.module(name, [
    centres.name,
    studies.name,
    users.name,
    'biobank.common',
    'biobank.users',
    'biobank.studies'
  ]);

  module
    .config(require('./states'))

    .directive('annotationTypeAdd',     require('./directives/annotationTypeAdd/annotationTypeAddDirective'))
    .directive('annotationTypeView',    require('./directives/annotationTypeView/annotationTypeViewDirective'))
    .directive('locationAdd',           require('./directives/locationAdd/locationAddDirective'))
    .directive('biobankAdmin',          require('./directives/biobankAdmin/biobankAdminDirective'))

    .component('userAdmin',             require('./components/users/userAdmin/userAdminComponent'))
    .component('usersTable',            require('./components/users/usersTable/usersTableComponent'))

    .component('annotationTypeSummary',
               require('./components/annotationTypeSummary/annotationTypeSummaryComponent'))

    .service('adminService',               require('./services/adminService'))

    .service('annotationTypeUpdateModal',
             require('./services/annotationTypeUpdateModal/annotationTypeUpdateModalService'))

    .factory('AnnotationTypeModals',   require('./services/AnnotationTypeModals'))

    .factory('ParticipantAnnotationTypeModals',
             require('./services/studies/ParticipantAnnotationTypeModals'))
    .factory('CollectionEventAnnotationTypeModals',
             require('./services/studies/CollectionEventAnnotationTypeModals'))
    .factory('SpecimenLinkAnnotationTypeModals',
             require('./services/studies/SpecimenLinkAnnotationTypeModals'))

    .factory('annotationTypeAddMixin', require('./services/annotationTypeAddMixin'));

  return {
    name: name,
    module: module
  };
});
