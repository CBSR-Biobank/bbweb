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

  return angular.module('admin.studies.specimenGroups', [
    'admin.studies.specimenGroups.controllers',
    'admin.studies.specimenGroups.states']);
});
