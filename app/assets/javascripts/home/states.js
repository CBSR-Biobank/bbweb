/**
 * Home routes.
 */
define(['angular'], function(angular) {
  'use strict';

  config.$inject = ['$urlRouterProvider', '$stateProvider'];

  function config($urlRouterProvider, $stateProvider) {

    $urlRouterProvider.otherwise('/index');

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
          displayName: 'Home'
        }
      })
      .state('home.about', {
        url: '^/about',
        views: {
          'main@': {
            templateUrl: '/assets/javascripts/home/about.html'
          }
        },
        data: {
          displayName: 'About'
        }
      })
      .state('home.contact', {
        url: '^/contact',
        views: {
          'main@': {
            templateUrl: '/assets/javascripts/home/contact.html'
          }
        },
        data: {
          displayName: 'Contact Us'
        }
      });
  }

  return config;
});
