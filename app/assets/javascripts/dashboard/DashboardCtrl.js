/**
 * Dashboard controllers.
 */
define([], function() {
  'use strict';

  DashboardCtrl.$inject = ['user'];

  function DashboardCtrl(user) {
    var vm = this;
    vm.user = user;
  }

});
