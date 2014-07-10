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

  return angular.module('admin.studies.processing', [
    'admin.studies.processing.controllers',
    'admin.studies.processing.states',
    'admin.studies.processing.helpers']);
});
