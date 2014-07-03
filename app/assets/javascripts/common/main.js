/**
 * Common functionality.
 */
define([
  'angular',
  './services/helpers',
  './services/playRoutes',
  './controllers/controllers',
  './filters',
  './directives/forms',
  './directives/uiBreadcrumbs',
  './directives/ngLoginSubmit'],
       function(angular) {
         'use strict';

         return angular.module(
           'biobank.common', [
             'common.helpers',
             'common.playRoutes',
             'common.filters',
             'common.directives.forms',
             'common.directives.uiBreadcrumbs',
             'common.directives.ngLoginSubmit']);
       });
