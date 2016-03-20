/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 *
 * Configures routes for the administration module.
 */
define(function () {
  'use strict';

  config.$inject = [
    '$urlRouterProvider',
    '$stateProvider',
    'authorizationProvider'
  ];

  function config($urlRouterProvider, $stateProvider, authorizationProvider ) {

    $urlRouterProvider.otherwise('/');

    $stateProvider.state('home.admin', {
      url: 'admin',
      resolve: {
        user: authorizationProvider.requireAuthenticatedUser
      },
      views: {
        'main@': {
          template: '<biobank-admin></biobank-admin>'
        }
      },
      data: {
        displayName: 'Administration'
      }
    });

  }

  return config;
});
