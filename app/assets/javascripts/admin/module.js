/** Study service */
define(['angular'], function(angular) {
  'use strict';

  return angular.module('biobank.admin', [
    'ui.router',
    'ngSanitize',
    'biobank.common',
    'biobank.users',
    'biobank.studies'
  ]);

});
