/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 *
 * Configures routes for the administration module.
 */
define(function () {
  'use strict';

  config.$inject = [
    '$urlRouterProvider',
    '$stateProvider',
    'authorizationProvider'
  ];

  function config($urlRouterProvider, $stateProvider, authorizationProvider ) {

    $urlRouterProvider.otherwise('/');

    $stateProvider.state('home.admin', {
      url: 'admin',
      views: {
        'main@': {
          templateUrl: '/assets/javascripts/admin/adminDetails.html',
          controller: 'AdminCtrl as vm'
        }
      },
      resolve: {
        user: authorizationProvider.requireAuthenticatedUser,
        aggregateCounts: ['adminService', function(adminService) {
          return adminService.aggregateCounts();
        }]
      },
      data: {
        displayName: 'Administration'
      }
    });

  }

  return config;
});
