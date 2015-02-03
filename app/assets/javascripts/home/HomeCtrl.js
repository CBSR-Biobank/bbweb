define(['./module'], function(module) {
  'use strict';

  module.controller('HomeCtrl', HomeCtrl);

  HomeCtrl.$inject = ['$rootScope', 'usersService'];

  /**
   * Controller for the index page.
   */
  function HomeCtrl($rootScope, usersService) {
    var vm = this;

    vm.userIsAuthenticated = usersService.isAuthenticated();

    $rootScope.pageTitle = 'Biobank';
  }

});
