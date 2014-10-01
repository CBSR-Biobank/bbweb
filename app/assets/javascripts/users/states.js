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
      },
      data: {
        notifications: ''
      }
    });

    $stateProvider.state('users.login.registered', {
      // does not define URL here so that it appears that shis is same page to the user as
      // 'users.login'
      views: {
        'main@': {
          templateUrl: '/assets/javascripts/users/login.html',
          controller: 'LoginCtrl as vm'
        }
      },
      data: {
        notifications: 'Your account was created and is now pending administrator approval.'
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
      },
      data: {
        notifications: ''
      }
    });

    $stateProvider.state('users.register.failed', {
      // does not define URL here so that it appears that shis is same page to the user as
      // 'users.login'
      views: {
        'main@': {
          templateUrl: '/assets/javascripts/users/registerUserForm.html',
          controller: 'RegisterUserCtrl as vm'
        }
      },
      data: {
        notifications: 'That email address is already registered.'
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
          controller: 'UserUpdateCtrl as vm'
        }
      },
      data: {
        displayName: 'User Settings'
      }
    });

  }

});
