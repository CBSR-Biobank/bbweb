/**
 * Configure routes for the administraiotn module.
 */
define(['./module'], function(module) {
  'use strict';

  module.config([
    '$urlRouterProvider', '$stateProvider', 'authorizationProvider',
    function($urlRouterProvider, $stateProvider, authorizationProvider ) {

      $urlRouterProvider.otherwise('/');

      $stateProvider.state('home.admin', {
        url: '^/admin',
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

      //.when('/users', {templateUrl:'/assets/javascripts/user/users.html', controller:controllers.UserCtrl})
      //.when('/users/:id', {templateUrl:'/assets/javascripts/user/editUser.html', controller:controllers.UserCtrl});
    }
  ]);

});
