/**
 * User package module.
 * Manages all sub-modules so other RequireJS modules only have to import the package.
 */
define([
  'angular',
  './states',
  './controllers',
  './services',
  './directives/studyTabs',
  './annotTypes/main'
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
    'study.services',
    'admin.studies.annotTypes']);
});
