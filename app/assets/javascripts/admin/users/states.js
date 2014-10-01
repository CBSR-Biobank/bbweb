/**
 * Configure routes of centres module.
 */
define(['../module'], function(module) {
  'use strict';

  module.config(config);

  config.$inject = [
    '$urlRouterProvider', '$stateProvider', 'userResolve'
  ];

  function config($urlRouterProvider, $stateProvider, userResolve ) {
    $urlRouterProvider.otherwise('/');

    /**
     * Displays all users in a table
     */
    $stateProvider.state('admin.users', {
      url: '/users',
      resolve: {
        user: userResolve.user
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
