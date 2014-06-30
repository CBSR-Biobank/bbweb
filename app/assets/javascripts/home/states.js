/**
 * Home routes.
 */
define(['angular'], function(angular, controllers) {
  'use strict';

  var mod = angular.module('home.states', ['ui.router', 'home.controllers', 'biobank.common']);
  mod.config(['$urlRouterProvider', '$stateProvider', function($urlRouterProvider, $stateProvider) {

    $urlRouterProvider.otherwise('/home');

    $stateProvider
      .state('home', {
        url: '/',
        views: {
          'main@': {
            templateUrl: '/assets/javascripts/home/home.html',
            controller: 'HomeCtrl'
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
  }]);
  return mod;
});
