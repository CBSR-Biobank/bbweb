/**
 * User package module.
 * Manages all sub-modules so other RequireJS modules only have to import the package.
 */
define(['angular', './states', './controllers', './services', './directives/studyTabs'], function(angular) {
  'use strict';

  return angular.module('biobank.admin.study', [
    'ngCookies',
    'ngRoute',
    'ui.bootstrap',
    'ngTable',
    'study.controllers',
    'study.states',
    'study.services',
    'study.directives.studyTabs']);
});
