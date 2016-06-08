/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  /**
   * Home page directive.
   */
  function homeDirective() {
    var directive = {
      restrict: 'E',
      templateUrl : '/assets/javascripts/home/directives/home/home.html',
      controller: HomeCtrl,
      controllerAs: 'vm'
    };

    return directive;
  }

  HomeCtrl.$inject = ['$rootScope', 'usersService'];

  function HomeCtrl($rootScope, usersService) {
    var vm = this;

    vm.userIsAuthenticated = false;
    $rootScope.pageTitle = 'Biobank';
    init();

    //--

    function init() {
      usersService.requestCurrentUser().then(function () {
        vm.userIsAuthenticated = true;
      });
    }
  }

  return homeDirective;

});
