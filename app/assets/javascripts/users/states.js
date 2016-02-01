/**
 * Configures routes of user module.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define([], function() {
  'use strict';

  config.$inject = ['$urlRouterProvider', '$stateProvider', 'authorizationProvider'];

  function config($urlRouterProvider, $stateProvider, authorizationProvider ) {

    $urlRouterProvider.otherwise('/');

    $stateProvider.state('home.users', {
      abstract: true,
      url: '^/users',
      views: {
        'main@': {
          template: '<ui-view></ui-view>'
        }
      }
    });

    $stateProvider.state('home.users.login', {
      url: '^/login',
      views: {
        'main@': {
          templateUrl: '/assets/javascripts/users/login.html',
          controller: 'LoginCtrl as vm'
        }
      }
    });

    $stateProvider.state('home.users.forgot', {
      url: '^/forgot',
      resolve: {
      },
      views: {
        'main@': {
          templateUrl: '/assets/javascripts/users/forgot.html',
          controller: 'ForgotPasswordCtrl as vm'
        }
      }
    });

    $stateProvider.state('home.users.forgot.passwordSent', {
      url: '^/passwordSent/{email}',
      views: {
        'main@': {
          templateUrl: '/assets/javascripts/users/passwordSent.html',
          controller: 'PasswordSentCtrl as vm'
        }
      }
    });

    $stateProvider.state('home.users.register', {
      url: '^/register',
      views: {
        'main@': {
          templateUrl: '/assets/javascripts/users/registerUserForm.html',
          controller: 'RegisterUserCtrl as vm'
        }
      }
    });

    /**
     * Allows changes to be made to a user
     */
    $stateProvider.state('home.users.profile', {
      url: '^/profile',
      resolve: {
        user: authorizationProvider.requireAuthenticatedUser
      },
      views: {
        'main@': {
          templateUrl: '/assets/javascripts/users/userProfile.html',
          controller: 'UserProfileCtrl as vm'
        }
      },
      data: {
        displayName: 'User profile'
      }
    });

  }

  return config;
});
