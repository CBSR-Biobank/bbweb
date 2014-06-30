/**
 * Administration package module.

 * Manages all sub-modules so other RequireJS modules only have to import the package.
 */
define(['angular', './controllers', './states', './studies/main'], function(angular) {
  'use strict';

  return angular.module('biobank.admin', [
    'admin.controllers',
    'admin.states',
    'biobank.admin.study'
  ]);
});
