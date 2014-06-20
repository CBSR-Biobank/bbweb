/**
 * User package module.
 * Manages all sub-modules so other RequireJS modules only have to import the package.
 */
define(['angular', './routes', './controllers', './services', './directives/studyTabs'], function(angular) {
  'use strict';

  return angular.module('biobank.study', [
    'ngCookies',
    'ngRoute',
    'ui.bootstrap',
    'ngTable',
    'study.controllers',
    'study.routes',
    'study.services',
    'study.directives.studyTabs']);
});
