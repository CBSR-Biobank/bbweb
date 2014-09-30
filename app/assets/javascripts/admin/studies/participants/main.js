/**
 * User package module.
 * Manages all sub-modules so other RequireJS modules only have to import the package.
 */
define([
  'angular',
  './states'
], function(angular) {
  'use strict';

  return angular.module('admin.studies.participants', [
    'admin.studies.participants.states'
  ]);
});
