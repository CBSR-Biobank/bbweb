/**
 * User package module.
 * Manages all sub-modules so other RequireJS modules only have to import the package.
 */
define([
  'angular',
  './controllers',
  './directives',
  './states',
  './services'
], function(angular) {
  'use strict';

  return angular.module('biobank.users', [
    'users.controllers',
    'users.directives',
    'users.states',
    'users.services'
  ]);
});
