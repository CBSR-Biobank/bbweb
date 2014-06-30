/**
 * Common functionality.
 */
define([
  'angular',
  './services/helper',
  './services/playRoutes',
  './controllers/errorModal',
  './filters',
  './directives/forms',
  './directives/uiBreadcrumbs',
  './directives/ngLoginSubmit'],
       function(angular) {
         'use strict';

         return angular.module(
           'biobank.common', [
             'common.helper',
             'common.playRoutes',
             'common.errorModal',
             'common.filters',
             'common.directives.forms',
             'common.directives.uiBreadcrumbs',
             'common.directives.ngLoginSubmit']);
       });
