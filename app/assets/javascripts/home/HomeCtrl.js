/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['angular'], function(angular) {
  'use strict';

  HomeCtrl.$inject = ['$rootScope', 'usersService'];

  /**
   * Controller for the index page.
   */
  function HomeCtrl($rootScope, usersService) {
    var vm = this;
    vm.userIsAuthenticated = usersService.isAuthenticated();
    $rootScope.pageTitle = 'Biobank';
  }

  return HomeCtrl;
});
