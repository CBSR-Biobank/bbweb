/**
 * Configure routes of centres module.
 */
define(['../module'], function(module) {
  'use strict';

  module.config(config);

  config.$inject = [
    '$urlRouterProvider', '$stateProvider', 'authorizationProvider'
  ];

  function config($urlRouterProvider, $stateProvider, authorizationProvider) {
    $urlRouterProvider.otherwise('/');

    resolveUserCount.$inject = ['usersService'];
    function resolveUserCount(usersService) {
      return usersService.getUserCount();
    }

    /**
     * Displays all users in a table
     */
    $stateProvider.state('home.admin.users', {
      url: '/users',
      resolve: {
        user: authorizationProvider.requireAuthenticatedUser,
        userCount : resolveUserCount
      },
      views: {
        'main@': {
          templateUrl: '/assets/javascripts/admin/users/usersTable.html',
          controller: 'UsersTableCtrl as vm'
        }
      },
      data: {
        displayName: 'Users'
      }
    });
  }
});
