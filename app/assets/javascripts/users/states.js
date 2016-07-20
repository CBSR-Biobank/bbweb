/**
 * Configures routes of user module.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  config.$inject = ['$urlRouterProvider', '$stateProvider'];

  function config($urlRouterProvider, $stateProvider) {

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
          template: '<login></login>'
        }
      }
    });

    $stateProvider.state('home.users.forgot', {
      url: '^/forgot',
      resolve: {
      },
      views: {
        'main@': {
          template: '<forgot-password></forgot-password>'
        }
      }
    });

    $stateProvider.state('home.users.forgot.passwordSent', {
      url: '^/passwordSent/{email}',
      views: {
        'main@': {
          template: '<password-sent email="vm.email"></password-sent>',
          controller: [
            '$stateParams',
            function ($stateParams) {
              this.email = $stateParams.email;
            }
          ],
          controllerAst: 'vm'
        }
      }
    });

    $stateProvider.state('home.users.register', {
      url: '^/register',
      views: {
        'main@': {
          template: '<register-user></register-user>'
        }
      }
    });

    /**
     * Allows changes to be made to a user
     */
    $stateProvider.state('home.users.profile', {
      url: '^/profile',
      views: {
        'main@': {
          template: '<user-profile></user-profile>'
        }
      },
      data: {
        displayName: 'User profile'
      }
    });

  }

  return config;
});
