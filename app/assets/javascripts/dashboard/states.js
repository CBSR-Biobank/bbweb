/**
 * Dashboard routes.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  config.$inject = ['$urlRouterProvider', '$stateProvider'];

  function config($urlRouterProvider, $stateProvider) {

    $urlRouterProvider.otherwise('/');

    $stateProvider.state('home.dashboard', {
      url: '^/dashboard',
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

  return config;
});
