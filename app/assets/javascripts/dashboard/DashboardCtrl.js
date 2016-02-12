/**
 * Dashboard controller.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  DashboardCtrl.$inject = ['user'];

  function DashboardCtrl(user) {
    var vm = this;
    vm.user = user;
  }

});
