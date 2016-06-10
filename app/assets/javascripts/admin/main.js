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

  module.config(require('./states'));

  module.directive('annotationTypeAdd',     require('./directives/annotationTypeAdd/annotationTypeAddDirective'));
  module.directive('annotationTypeView',    require('./directives/annotationTypeView/annotationTypeViewDirective'));
  module.directive('annotationTypeSummary', require('./directives/annotationTypeSummary/annotationTypeSummaryDirective'));
  module.directive('locationAdd',           require('./directives/locationAdd/locationAddDirective'));
  module.directive('biobankAdmin',          require('./directives/biobankAdmin/biobankAdminDirective'));
  module.directive('statusLine',            require('./directives/statusLine/statusLineDirective'));

  module.component('userAdmin',             require('./components/users/userAdmin/userAdminComponent'));
  module.component('usersTable',            require('./components/users/usersTable/usersTableComponent'));

  module.service('adminService', require('./services/adminService'));

  module.factory('AnnotationTypeModals',   require('./services/AnnotationTypeModals'));

  module.factory('ParticipantAnnotationTypeModals',
                 require('./services/studies/ParticipantAnnotationTypeModals'));
  module.factory('CollectionEventAnnotationTypeModals',
                 require('./services/studies/CollectionEventAnnotationTypeModals'));
  module.factory('SpecimenLinkAnnotationTypeModals',
                 require('./services/studies/SpecimenLinkAnnotationTypeModals'));

  module.factory('annotationTypeAddMixin', require('./services/annotationTypeAddMixin'));

  return {
    name: name,
    module: module
  };
});
