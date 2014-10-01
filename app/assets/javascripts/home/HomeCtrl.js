define(['./module'], function(module) {
  'use strict';

  module.controller('HomeCtrl', HomeCtrl);

  HomeCtrl.$inject = ['$rootScope'];

  /**
   * Controller for the index page.
   */
  function HomeCtrl($rootScope) {
    $rootScope.pageTitle = 'Biobank';
  }

});
