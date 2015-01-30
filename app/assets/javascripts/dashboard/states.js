/**
 * Dashboard routes.
 */
define(['./module'], function(module) {
  'use strict';

  module.config(config);

  config.$inject = ['$urlRouterProvider', '$stateProvider', 'userResolve'];

  function config($urlRouterProvider, $stateProvider, userResolve) {

    $urlRouterProvider.otherwise('/');

    $stateProvider.state('home.dashboard', {
      url: '^/dashboard',
      resolve: {
        user: userResolve.user
      },
      views: {
        'main@': {
          templateUrl: '/assets/javascripts/dashboard/dashboard.html',
          controller: 'DashboardCtrl'
        }
      },
      data: {
        displayName: 'Dashboard'
      }
    });

    $stateProvider.state('home.dashboard.admin', {
      url: '^/admin',
      resolve: {
        user: userResolve.user
      },
      views: {
        'main@': {
          templateUrl: '/assets/javascripts/dashboard/dashboard.html',
          controller: 'DashboardCtrl'
        }
      },
      data: {
        displayName: 'Admin'
      }
    });
  }

});
