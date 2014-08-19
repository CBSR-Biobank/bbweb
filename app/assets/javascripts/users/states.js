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

      //.when('/users', {templateUrl:'/assets/javascripts/user/users.html', controller:controllers.UserCtrl})
      //.when('/users/:id', {templateUrl:'/assets/javascripts/user/editUser.html', controller:controllers.UserCtrl});
    }]);

  return mod;
});
