/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 *
 * Configures routes for the administration module.
 */
define(function () {
  'use strict';

  config.$inject = ['$stateProvider'];

  function config($stateProvider) {

    $stateProvider.state('home.admin', {
        // this state is checked for an authorized user, see uiRouterIsAuthorized() in app.js
      url: 'admin',
      views: {
        'main@': 'biobankAdmin'
      }
    });

  }

  return config;
});
