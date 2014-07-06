/**
 * User package module.
 * Manages all sub-modules so other RequireJS modules only have to import the package.
 */
define([
  'angular',
  './controllers',
  './states',
  './helpers'
], function(angular) {
  'use strict';

  return angular.module('admin.studies.ceventTypes', [
    'admin.studies.ceventTypes.controllers',
    'admin.studies.ceventTypes.states',
    'admin.studies.ceventTypes.helpers']);
});
