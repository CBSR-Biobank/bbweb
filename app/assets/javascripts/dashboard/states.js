/**
 * Dashboard routes.
 */
define(['angular', './controllers', 'common'], function(angular, controllers) {
  'use strict';

  var mod = angular.module('dashboard.states', ['ui.router', 'biobank.common']);
  mod.config([
    '$urlRouterProvider', '$stateProvider', 'userResolve',
    function($urlRouterProvider, $stateProvider, userResolve) {

      $urlRouterProvider.otherwise('/');

      $stateProvider
        .state('dashboard', {
          url: '/dashboard',
          views: {
            'main@': {
              templateUrl: '/assets/javascripts/dashboard/dashboard.html',
              controller:controllers.DashboardCtrl
            }
          },
          resolve: {
            user: userResolve.user
          },
          data: {
            displayName: false
          }
        })
        .state('dashboard.admin', {
          url: '/dashboard/admin',
          views: {
            'main@': {
              templateUrl: '/assets/javascripts/dashboard/dashboard.html',
              controller:controllers.AdminDashboardCtrl
            }
          },
          resolve: {
            user: userResolve.user
          },
          data: {
            displayName: false
          }
        });
    }]);
  return mod;
});
