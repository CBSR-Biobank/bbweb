/**
 * Dashboard shown after user is logged in.
 * dashboard/main.js is the entry module which serves as an entry point so other modules only have
 * to include a single module.
 */
define([
  'angular',
  './DashboardCtrl',
  './states',
], function(angular,
            DashboardCtrl,
            states) {
  'use strict';

  var module = angular.module('biobank.dashboard', []);

  module.controller('DashboardCtrl', DashboardCtrl);
  module.config(states);

  return module;
});
