/**
 * Configures routes of user module.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  config.$inject = [
    '$urlRouterProvider',
    '$stateProvider'
  ];

  function config($urlRouterProvider, $stateProvider) {
    $urlRouterProvider.otherwise('/');

    $stateProvider
      .state('home.users', {
        abstract: true,
        url: '^/users',
        views: {
          'main@': {
            template: '<ui-view></ui-view>'
          }
        }
      })
      .state('home.users.login', {
        url: '^/login',
        views: {
          'main@': {
            template: '<login></login>'
          }
        }
      })
      .state('home.users.forgot', {
        url: '^/forgot',
        resolve: {
        },
        views: {
          'main@': {
            template: '<forgot-password></forgot-password>'
          }
        }
      })
      .state('home.users.forgot.passwordSent', {
        url: '^/passwordSent/{email}',
        views: {
          'main@': {
            template: '<password-sent email="vm.email"></password-sent>',
            controller: [
              '$transition$',
              function ($transition$) {
                this.email = $transition$.params().email;
              }
            ],
            controllerAst: 'vm'
          }
        }
      })
      .state('home.users.register', {
        url: '^/register',
        views: {
          'main@': {
            template: '<register-user></register-user>'
          }
        }
      })
      .state('home.users.profile', {
        url: '^/profile',
        views: {
          'main@': {
            template: '<user-profile></user-profile>'
          }
        }
      });

  }

  return config;
});
