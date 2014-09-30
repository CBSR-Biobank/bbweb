/**
 * User package module.
 * Manages all sub-modules so other RequireJS modules only have to import the package.
 */
define(['angular', './controllers', ], function(angular) {
  'use strict';

  return angular.module('admin.studies.annotationTypes', [
    'admin.studies.annotationTypes.controllers'
  ]);
});
