/**
 * Dashboard controllers.
 */
define(['./module'], function(module) {
  'use strict';

  module.controller('DashboardCtrl', DashboardCtrl);

  DashboardCtrl.$inject = ['user'];

  function DashboardCtrl(user) {
    var vm = this;
    vm.user = user;
  }

});
