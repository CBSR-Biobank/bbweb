/**
 * User package module.
 * Manages all sub-modules so other RequireJS modules only have to import the package.
 */
define([
  'angular',
  './states',
  './controllers',
  './services/services',
  './services/helpers',
  './directives/studyTabs',
  './annotTypes/main',
  './specimenGroups/main'
], function(angular) {
  'use strict';

  return angular.module('biobank.admin.study', [
    'ngCookies',
    'ngRoute',
    'ui.bootstrap',
    'ngTable',
    'admin.studies.controllers',
    'admin.studies.states',
    'admin.studies.directives.studyTabs',
    'studies.services',
    'studies.helpers',
    'admin.studies.annotTypes',
    'admin.studies.specimenGroups']);
});
