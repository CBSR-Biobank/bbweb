/**
 * Common functionality.
 */
define([
  'angular',
  './services/helpers',
  './services/playRoutes',
  './controllers/controllers',
  './filters',
  './directives/directives',
  './directives/uiBreadcrumbs'],
       function(angular) {
         'use strict';

         return angular.module(
           'biobank.common', [
             'common.helpers',
             'common.playRoutes',
             'common.filters',
             'common.directives.directives',
             'common.directives.uiBreadcrumbs']);
       });
