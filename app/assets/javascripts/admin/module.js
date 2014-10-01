/** Study service */
define(['angular'], function(angular) {
  'use strict';

  return angular.module('biobank.admin', [
    'ui.router',
    'biobank.users',
    'biobank.studies'
  ]);

});
