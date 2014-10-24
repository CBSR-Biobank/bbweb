/** Study service */
define(['angular', 'angular-ui-router', 'angular-sanitize'], function(angular) {
  'use strict';

  return angular.module('biobank.admin', [
    'ui.router',
    'ngSanitize',
    'biobank.common',
    'biobank.users',
    'biobank.studies'
  ]);

});
