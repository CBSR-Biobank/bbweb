/**
 * Dashboard routes.
 */
define(['angular', './controllers', 'common'], function(angular, controllers) {
  'use strict';

  var mod = angular.module('dashboard.states', ['ui.router', 'biobank.common']);
  mod.config(['$stateProvider', 'userResolve', function($stateProvider, userResolve) {
    $stateProvider
      .state('dashboard', {
        url: '/dashboard',
        views: {
          'main@': {
            templateUrl: '/assets/javascripts/dashboard/dashboard.html',
            controller:controllers.DashboardCtrl
          }
        },
        resolve:userResolve,
        data: {
          displayName: false
        }
      })
      .state('dashboard.admin', {
        url: '/admin',
        views: {
          'main@': {
            templateUrl: '/assets/javascripts/dashboard/dashboard.html',
            controller:controllers.AdminDashboardCtrl
          }
        },
        resolve:userResolve,
        data: {
          displayName: false
        }
      });
  }]);
  return mod;
});
