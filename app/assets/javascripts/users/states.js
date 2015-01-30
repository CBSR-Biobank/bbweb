/**
 * Configure routes of user module.
 */
define(['./module'], function(module) {
  'use strict';

  module.config(config);

  config.$inject = ['$urlRouterProvider', '$stateProvider', 'userResolve'];

  function config($urlRouterProvider, $stateProvider, userResolve ) {

    $urlRouterProvider.otherwise('/');

    $stateProvider.state('home.users', {
      abstract: true,
      url: '^/users',
      views: {
        'main@': {
          template: '<ui-view/>'
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
      },
      data: {
        emailNotFound: false
      }
    });

    $stateProvider.state('home.users.forgot.emailNotFound', {
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
