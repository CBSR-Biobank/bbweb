/**
 * User package module.
 * Manages all sub-modules so other RequireJS modules only have to import the package.
 */
define([
  'angular',
  './controllers',
  './states',
  './services'
], function(angular) {
  'use strict';

  return angular.module('biobank.users', [
    'ngCookies',
    'ngRoute',
    'users.controllers',
    'users.states',
    'users.services'
  ]);
});
