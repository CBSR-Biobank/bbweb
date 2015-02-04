/**
 * collection routes.
 */
define(['./module'], function(module) {
  'use strict';

  module.config(config);

  config.$inject = ['$urlRouterProvider', '$stateProvider', 'authorizationProvider'];

  function config($urlRouterProvider, $stateProvider, authorizationProvider) {

    resolveStudyCounts.$inject = ['studiesService'];
    function resolveStudyCounts(studiesService) {
      return studiesService.getStudyCounts();
    }

    $urlRouterProvider.otherwise('/');

    $stateProvider.state('home.collection', {
      url: '^/collection',
      resolve: {
        user: authorizationProvider.requireAuthenticatedUser,
        studyCounts: resolveStudyCounts
      },
      views: {
        'main@': {
          templateUrl: '/assets/javascripts/collection/collection.html',
          controller: 'CollectionCtrl as vm'
        }
      },
      data: {
        displayName: 'Collection'
      }
    });
  }

});
