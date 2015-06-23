/**
 * Dashboard shown after user is logged in.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
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
