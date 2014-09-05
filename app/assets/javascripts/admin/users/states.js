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

      /**
       * Allows changes to be made to a user
       */
      $stateProvider.state('admin.users.user', {
        url: '/{userId}',
        resolve: {
          user: userResolve.user,
          userToModify: ['$stateParams', 'userService', function($stateParams, userService) {
            if ($stateParams.userId) {
              return userService.query($stateParams.userId);
            }
            throw new Error("state parameter userId is invalid");
          }]
        },
        views: {
          'main@': {
            templateUrl: '/assets/javascripts/admin/users/userForm.html',
            controller: 'UserUpdateCtrl'
          }
        },
        data: {
          displayName: 'Users'
        }
      });
    }]);

  return mod;
});
