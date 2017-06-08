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

  HomeCtrl.$inject = ['$rootScope', '$timeout', 'usersService', 'User'];

  function HomeCtrl($rootScope, $timeout, usersService, User) {
    var vm = this;

    vm.userIsAuthenticated = false;
    $rootScope.pageTitle = 'Biobank';
    init();

    //--

    function init() {
      usersService.requestCurrentUser().then(function (user) {
        vm.user = User.create(user);
        vm.userIsAuthenticated = true;
        vm.allowCollection = vm.user.hasRole('SpecimenCollector');
        vm.shippingAllowed = vm.user.hasRole('ShippingUser');
        vm.adminAllowed = vm.user.hasAnyRoleOf('StudyAdministrator',
                                               'CentreAdministrator',
                                               'UserAdministrator');
      });
    }
  }

  return homeDirective;

});
