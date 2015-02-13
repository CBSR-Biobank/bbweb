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

    resolveUserCounts.$inject = ['usersService'];
    function resolveUserCounts(usersService) {
      return usersService.getUserCounts();
    }

    /**
     * Displays all users in a table
     */
    $stateProvider.state('home.admin.users', {
      url: '/users',
      resolve: {
        user: authorizationProvider.requireAuthenticatedUser,
        userCounts: resolveUserCounts
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
