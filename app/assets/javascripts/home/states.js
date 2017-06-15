/**
 * Home module routes.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['angular'], function(angular) {
  'use strict';

  config.$inject = [
    '$urlRouterProvider',
    '$stateProvider'
  ];

  function config($urlRouterProvider, $stateProvider) {

    $urlRouterProvider.otherwise('/#');

    $stateProvider
      .state('home', {
        url: '/',
        views: {
          'main@': {
            template: '<home></home>'
          }
        }
      })
      .state('home.about', {
        url: 'about',
        views: {
          'main@': {
            templateUrl: '/assets/javascripts/home/about.html'
          }
        }
      })
      .state('home.contact', {
        url: 'contact',
        views: {
          'main@': {
            templateUrl: '/assets/javascripts/home/contact.html'
          }
        }
      })
      .state('404', {
        template: '<div>error</div>'
      });
  }

  return config;
});
