/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  var component = {
    templateUrl : '/assets/javascripts/admin/users/components/userAdmin/userAdmin.html',
    controller: UserAdminController,
    controllerAs: 'vm'
  };

  UserAdminController.$inject = ['UserCounts', 'breadcrumbService'];

  /*
   * Shows a table of users.
   */
  function UserAdminController(UserCounts, breadcrumbService) {
    var vm = this;
    vm.$onInit = onInit;

    //--

    function onInit() {
      vm.breadcrumbs = [
        breadcrumbService.forState('home'),
        breadcrumbService.forState('home.admin'),
        breadcrumbService.forState('home.admin.users')
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
