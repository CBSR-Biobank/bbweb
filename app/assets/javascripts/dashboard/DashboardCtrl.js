/**
 * Dashboard controllers.
 */
define(['./module'], function(module) {
  'use strict';

  module.controller('DashboardCtrl', DashboardCtrl);

  DashboardCtrl.$inject = ['user'];

  /**
   * user is not a service, but stems from userResolve (Check ../users/services.js) object used by dashboard.routes.
   */
  function DashboardCtrl($scope, user) {
    var vm = this;
    vm.user = user;
  }

});
