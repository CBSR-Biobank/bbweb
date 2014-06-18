/**
 * Common functionality.
 */
define([
  'angular',
  './services/helper',
  './services/playRoutes',
  './services/versionMismatchModal',
  './filters',
  './directives/example',
  './directives/ngLoginSubmit'],
       function(angular) {
         'use strict';

         return angular.module(
           'biobank.common', [
             'common.helper',
             'common.playRoutes',
             'common.versionMismatchModal',
             'common.filters',
             'common.directives.example',
             'common.directives.ngLoginSubmit']);
       });
