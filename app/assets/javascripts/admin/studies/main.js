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
  './participants/main',
  './specimenGroups/main',
  './ceventTypes/main'
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
    'admin.studies.helpers',
    'admin.studies.participants',
    'admin.studies.specimenGroups',
    'admin.studies.ceventTypes']);
});
