/**
 * User package module.
 * Manages all sub-modules so other RequireJS modules only have to import the package.
 */
define(['angular', './states', './services'], function(angular) {
  'use strict';

  return angular.module('biobank.user', ['ngCookies', 'ngRoute', 'user.states', 'user.services']);
});
