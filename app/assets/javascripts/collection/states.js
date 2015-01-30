/**
 * collection routes.
 */
define(['./module'], function(module) {
  'use strict';

  module.config(config);

  config.$inject = ['$urlRouterProvider', '$stateProvider', 'userResolve'];

  function config($urlRouterProvider, $stateProvider, userResolve) {

    $urlRouterProvider.otherwise('/');

    $stateProvider.state('home.collection', {
      url: '^/collection',
      resolve: {
        user: userResolve.user
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
