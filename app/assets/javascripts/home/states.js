/**
 * Home module routes.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function() {
  'use strict';

  /* @ngInject */
  function config($stateProvider) {
    $stateProvider
      .state('home', {
        url: '/',
        views: {
          'main@': 'home'
        }
      })
      .state('home.about', {
        url: 'about',
        views: {
          'main@': 'about'
        }
      })
      .state('home.contact', {
        url: 'contact',
        views: {
          'main@': {
            template: require('./contact.html')
          }
        }
      })
      .state('404', {
        template: '<div>error</div>'
      });
  }

  return config;
});
