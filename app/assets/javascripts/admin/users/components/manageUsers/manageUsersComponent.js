/**
 *
 */
define(function () {
  'use strict';

  var component = {
    templateUrl: '/assets/javascripts/admin/users/components/manageUsers/manageUsers.html',
    controller: Controller,
    controllerAs: 'vm',
    bindings: {
    }
  };

  Controller.$inject = [
    'breadcrumbService',
    'usersService',
    'UserCounts'
   ];

  /*
   * Controller for this component.
   */
  function Controller(breadcrumbService,
                      usersService,
                      UserCounts) {
    var vm = this;
    vm.$onInit = onInit;

    //--

    function onInit() {
      vm.breadcrumbs = [
        breadcrumbService.forState('home'),
        breadcrumbService.forState('home.admin'),
        breadcrumbService.forState('home.admin.users'),
        breadcrumbService.forState('home.admin.users.manage')
      ];

      vm.haveUsers = false;

      UserCounts.get().then(function (counts) {
        vm.userCounts = counts;
        vm.haveUsers  = (vm.userCounts.total > 0);
      });
    }
  }

  return component;
});
