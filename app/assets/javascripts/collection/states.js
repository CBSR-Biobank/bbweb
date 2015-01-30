/**
 * collection routes.
 */
define(['./module'], function(module) {
  'use strict';

  module.config(config);

  config.$inject = ['$urlRouterProvider', '$stateProvider', 'authorizationProvider'];

  function config($urlRouterProvider, $stateProvider, authorizationProvider) {

    $urlRouterProvider.otherwise('/');

    $stateProvider.state('home.collection', {
      url: '^/collection',
      resolve: {
        user: authorizationProvider.requireAuthenticatedUser
      },
      views: {
        'main@': {
          templateUrl: '/assets/javascripts/collection/collection.html',
          controller: 'CollectionCtrl'
        }
      },
      data: {
        displayName: 'Collection'
      }
    });
  }

});
