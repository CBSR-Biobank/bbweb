/**
 * Study package module.
 * Manages all sub-modules so other RequireJS modules only have to import the package.
 */
define([
  'angular',
  './states',
  './controllers',
  './helpers'
  //'./services/services',
], function(angular) {
  'use strict';

  return angular.module('biobank.admin.users', [
    'admin.users.controllers',
    'admin.users.states',
    'admin.users.helpers',
    // 'users.services'
  ]);
});
