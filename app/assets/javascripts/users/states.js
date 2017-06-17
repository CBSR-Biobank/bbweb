/**
 * Configures routes of user module.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  config.$inject = [ '$stateProvider' ];

  function config($stateProvider) {
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
            component: 'forgotPassword'
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
            component: 'registerUser'
          }
        }
      })
      .state('home.users.profile', {
        url: '^/profile',
        views: {
          'main@': {
            component: 'userProfile'
          }
        }
      });

  }

  return config;
});
