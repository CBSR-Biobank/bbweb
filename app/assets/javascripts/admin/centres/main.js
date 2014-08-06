/**
 * Study package module.
 * Manages all sub-modules so other RequireJS modules only have to import the package.
 */
define([
  'angular',
  './states',
  './controllers',
  './services/services',
  './services/helpers'
], function(angular) {
  'use strict';

  return angular.module('biobank.admin.centres', [
    'ngCookies',
    'ngRoute',
    'ui.bootstrap',
    'ngTable',
    'admin.centres.controllers',
    'admin.centres.states',
    'centres.services']);
});
