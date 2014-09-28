/**
 * Configure routes of centres module.
 */
define(['angular'], function(angular) {
  'use strict';

  var mod = angular.module('admin.users.states', [
    'ui.router',
    'users.services',
    //'admin.centres.controllers',
    //'centres.services'
  ]);

  mod.config([
    '$urlRouterProvider', '$stateProvider', 'userResolve',
    function($urlRouterProvider, $stateProvider, userResolve ) {

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
            controller: 'UsersTableCtrl'
          }
        },
        data: {
          displayName: 'Users'
        }
      });
    }
  ]);

  return mod;
});
