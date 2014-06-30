/**
 * Home routes.
 */
define(['angular', './controllers', 'common'], function(angular, controllers) {
  'use strict';

  var mod = angular.module('home.states', ['ui.router', 'biobank.common']);
  mod.config(function($urlRouterProvider, $stateProvider) {

    $urlRouterProvider.otherwise('/home');

    $stateProvider
      .state('home', {
        url: '/',
        views: {
          'main@': {
            templateUrl: '/assets/javascripts/home/home.html',
            controller: controllers.HomeCtrl
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
            templateUrl: '/assets/javascripts/home/about.html',
            controller: controllers.HomeCtrl
          }
        },
        data: {
          displayName: 'About'
        }
      })
      .state('contact', {
        url: '/contact',
        views: {
          'main@': {
            templateUrl: '/assets/javascripts/home/contact.html',
            controller: controllers.HomeCtrl
          }
        },
        data: {
          displayName: 'Contact us'
        }
      });
  });
  return mod;
});
