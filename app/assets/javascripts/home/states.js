/**
 * Home routes.
 */
define(['./module'], function(module) {
  'use strict';

  module.config(config);

  config.$inject = ['$urlRouterProvider', '$stateProvider'];

  function config($urlRouterProvider, $stateProvider) {

    $urlRouterProvider.otherwise('/');

    $stateProvider
      .state('home', {
        url: '/',
        views: {
          'main@': {
            templateUrl: '/assets/javascripts/home/home.html',
            controller: 'HomeCtrl as vm'
          }
        },
        data: {
          displayName: false
        }
      })
      .state('about', {
        url: '/about',
        views: {
          'main@': {
            templateUrl: '/assets/javascripts/home/about.html'
          }
        },
        data: {
          displayName: false
        }
      })
      .state('contact', {
        url: '/contact',
        views: {
          'main@': {
            templateUrl: '/assets/javascripts/home/contact.html'
          }
        },
        data: {
          displayName: false
        }
      });
  }

});
