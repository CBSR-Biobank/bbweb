/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  UserAdminController.$inject = ['UserCounts'];

  /**
   *
   */
  function UserAdminController(UserCounts) {
    var vm = this;

    vm.$onInit = onInit;
    vm.haveUsers = false;

    //--

    function onInit() {
      UserCounts.get().then(function (counts) {
        vm.userCounts = counts;
        vm.haveUsers  = (vm.userCounts.total > 0);
      });
    }
  }

  return {
    templateUrl : '/assets/javascripts/admin/components/users/userAdmin/userAdmin.html',
    controller: UserAdminController,
    controllerAs: 'vm'
  };
});
