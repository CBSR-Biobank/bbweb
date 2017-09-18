/**
 *
 */
define(function () {
  'use strict';

  var component = {
    templateUrl: '/assets/javascripts/admin/users/components/membershipView/membershipView.html',
    controller: MembershipViewController,
    controllerAs: 'vm',
    bindings: {
      membership: '<'
    }
  };

  MembershipViewController.$inject = [];

  /*
   *
   */
  function MembershipViewController() {
    var vm = this;
    vm.$onInit = onInit;

    //--

    function onInit() {
      console.log(vm.membership);
    }
  }

  return component;
});
