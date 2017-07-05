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
          'main@': 'uiView'
        }
      })
      .state('home.users.login', {
        url: '^/login',
        views: {
          'main@': 'login'
        }
      })
      .state('home.users.forgot', {
        url: '^/forgot',
        resolve: {
        },
        views: {
          'main@': 'forgotPassword'
        }
      })
      .state('home.users.forgot.passwordSent', {
        url: '^/passwordSent/{email}',
        views: {
          'main@': 'passwordSent'
        }
      })
      .state('home.users.register', {
        url: '^/register',
        views: {
          'main@': 'registerUser'
        }
      });

  }

  return config;
});
