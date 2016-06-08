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
    '$stateProvider'
  ];

  function config($urlRouterProvider, $stateProvider) {

    $urlRouterProvider.otherwise('/');

    $stateProvider.state('home.admin', {
      url: 'admin',
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
