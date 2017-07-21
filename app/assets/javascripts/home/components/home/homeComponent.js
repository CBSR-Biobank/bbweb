/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  var component = {
    templateUrl : '/assets/javascripts/home/components/home/home.html',
    controller: HomeController,
    controllerAs: 'vm',
    bindings: {
    }
  };

  HomeController.$inject = ['$rootScope', '$timeout', 'usersService', 'User', 'breadcrumbService'];

  /*
   * Controller for this component.
   */
  function HomeController($rootScope, $timeout, usersService, User, breadcrumbService) {
    var vm = this;
    vm.$onInit = onInit;

    //--

    function onInit() {
      vm.breadcrumbs = [ breadcrumbService.forState('home') ];

      vm.userIsAuthenticated = false;
      $rootScope.pageTitle = 'Biobank';

      usersService.requestCurrentUser().then(function (user) {
        vm.user = user;
        vm.userIsAuthenticated = true;
        vm.allowCollection = vm.user.hasRole('SpecimenCollector');
        vm.shippingAllowed = vm.user.hasRole('ShippingUser');
        vm.adminAllowed = vm.user.hasAnyRoleOf('StudyAdministrator',
                                               'CentreAdministrator',
                                               'UserAdministrator');
      });
    }
  }

  return component;
});
