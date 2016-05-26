/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
*/
define(function (require) {
  'use strict';

  var angular = require('angular'),
      name = 'biobank.admin',
      module,
      centres = require('./centres/main'),
      studies = require('./studies/main'),
      users = require('./users/main'),
      directivesCentres = require('./directives/centres/main'),
      directivesStudies = require('./directives/studies/main'),
      services = require('./services/main');

  module = angular.module(name, [
    centres.name,
    studies.name,
    users.name,
    directivesCentres.name,
    directivesStudies.name,
    services.name,
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

  return {
    name: name,
    module: module
  };
});
