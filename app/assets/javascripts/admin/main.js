/**
 * Administration package module.

 * Manages all sub-modules so other RequireJS modules only have to import the package.
 */
define([
  'angular',
  './controllers',
  './states',
  './services',
  './centres/main',
  './studies/main',
  './users/main'
], function(angular) {
  'use strict';

  return angular.module('biobank.admin', [
    'admin.controllers',
    'admin.states',
    'admin.services',
    'biobank.admin.centres',
    'biobank.admin.studies',
    'biobank.admin.users'
  ]);
});
