/**
 * Configure routes of user module.
 */
define(['./module'], function(module) {
  'use strict';

  module.config(config);

  config.$inject = ['$urlRouterProvider', '$stateProvider', 'userResolve'];

  function config($urlRouterProvider, $stateProvider, userResolve ) {

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
          controller: 'LoginCtrl as vm'
        }
      }
    });

    $stateProvider.state('users.forgot', {
      url: '^/forgot',
      resolve: {
      },
      views: {
        'main@': {
          templateUrl: '/assets/javascripts/users/forgot.html',
          controller: 'ForgotPasswordCtrl as vm'
        }
      },
      data: {
        emailNotFound: false
      }
    });

    $stateProvider.state('users.forgot.emailNotFound', {
      url: '^/forgot',
      views: {
        'main@': {
          templateUrl: '/assets/javascripts/users/forgot.html',
          controller: 'ForgotPasswordCtrl as vm'
        }
      },
      data: {
        emailNotFound: true
      }
    });

    $stateProvider.state('users.forgot.passwordSent', {
      url: '^/passwordSent/{email}',
      views: {
        'main@': {
          templateUrl: '/assets/javascripts/users/passwordSent.html',
          controller: 'PasswordSentCtrl as vm'
        }
      }
    });

    $stateProvider.state('users.register', {
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
    $stateProvider.state('users.profile', {
      url: '^/profile',
      resolve: {
        user: userResolve.user
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

});
