/**
 * Configure routes of user module.
 */
define(['angular', 'common'], function(angular, controllers) {
  'use strict';

  var mod = angular.module('users.states', [
    'ui.router', 'users.controllers', 'users.services', 'biobank.common']);

  mod.config([
    '$urlRouterProvider', '$stateProvider', 'userResolve',
    function($urlRouterProvider, $stateProvider, userResolve ) {

      $urlRouterProvider.otherwise('/');

      $stateProvider.state('users', {
        abstract: true,
        url: '/users',
        views: {
          'main@': {
            template: '<ui-view/>'
          }
        }
      });

      $stateProvider.state('users.login', {
        url: '^/login',
        resolve: {
          notifications: function() { return null; }
        },
        views: {
          'main@': {
            templateUrl: '/assets/javascripts/users/login.html',
            controller: 'LoginCtrl'
          }
        }
      });

      $stateProvider.state('users.login.registered', {
        // does not define URL here so that it appears that shis is same page to the user as
        // "users.login"
        resolve: {
          notifications: function() {
            return 'Your account was created and is now pending administrator approval.';
          }
        },
        views: {
          'main@': {
            templateUrl: '/assets/javascripts/users/login.html',
            controller: 'LoginCtrl'
          }
        }
      });

      $stateProvider.state('users.forgot', {
        url: '^/forgot',
        resolve: {
          emailNotFound: function() {
            return false;
          }
        },
        views: {
          'main@': {
            templateUrl: '/assets/javascripts/users/forgot.html',
            controller: 'ForgotPasswordCtrl'
          }
        }
      });

      $stateProvider.state('users.forgot.emailNotFound', {
        url: '^/forgot',
        resolve: {
          emailNotFound: function() {
            return true;
          }
        },
        views: {
          'main@': {
            templateUrl: '/assets/javascripts/users/forgot.html',
            controller: 'ForgotPasswordCtrl'
          }
        }
      });

      $stateProvider.state('users.forgot.passwordSent', {
        url: '^/passwordSent/{email}',
        views: {
          'main@': {
            templateUrl: '/assets/javascripts/users/passwordSent.html',
            controller: 'ResetPasswordCtrl'
          }
        }
      });

      $stateProvider.state('users.register', {
        url: '^/register',
        resolve: {
          notifications: function() { return null; }
        },
        views: {
          'main@': {
            templateUrl: '/assets/javascripts/users/registerUserForm.html',
            controller: 'RegisterUserCtrl'
          }
        }
      });

      $stateProvider.state('users.register.failed', {
        // does not define URL here so that it appears that shis is same page to the user as
        // "users.register"
        resolve: {
          notifications: ['$stateParams', function() {
            return 'That email address is already registered.';
          }]
        },
        views: {
          'main@': {
            templateUrl: '/assets/javascripts/users/registerUserForm.html',
            controller: 'RegisterUserCtrl'
          }
        }
      });

      /**
       * Allows changes to be made to a user
       */
      $stateProvider.state('users.settings', {
        url: '^/settings',
        resolve: {
          user: userResolve.user
        },
        views: {
          'main@': {
            templateUrl: '/assets/javascripts/users/userSettingsForm.html',
            controller: 'UserUpdateCtrl'
          }
        },
        data: {
          displayName: 'User Settings'
        }
      });

      //.when('/users', {templateUrl:'/assets/javascripts/user/users.html', controller:controllers.UserCtrl})
      //.when('/users/:id', {templateUrl:'/assets/javascripts/user/editUser.html', controller:controllers.UserCtrl});
    }]);

  return mod;
});
