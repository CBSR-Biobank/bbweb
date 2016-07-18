/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(['lodash'], function (_) {
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

  HomeCtrl.$inject = ['$rootScope', '$timeout', 'usersService'];

  function HomeCtrl($rootScope, $timeout, usersService) {
    var vm = this;

    vm.userIsAuthenticated = false;
    $rootScope.pageTitle = 'Biobank';
    init();

    //--

    function init() {
      // A bit of a hack: We know that biobankHeaderDirective authenticates the user, so we use a timeout to
      // wait for authentication reply to have been resolved.
      $timeout(function () {
        vm.userIsAuthenticated = usersService.isAuthenticated();
      }, 50);
    }
  }

  return homeDirective;

});
