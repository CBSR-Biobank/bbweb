/**
 * User package module.
 * Manages all sub-modules so other RequireJS modules only have to import the package.
 */
define([
  'angular',
  './controllers',
  './states'
], function(angular) {
  'use strict';

  return angular.module('admin.studies.annotTypes', [
    'admin.studies.annotTypes.controllers',
    'admin.studies.annotTypes.states']);
});
