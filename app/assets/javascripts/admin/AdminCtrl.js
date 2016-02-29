/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  AdminCtrl.$inject = ['adminService'];

  /**
   * Administration controllers.
   *
   */
  function AdminCtrl(adminService) {
    var vm = this;

    vm.counts = {};

    init();

    //--

    function init() {
      adminService.aggregateCounts().then(function (aggregateCounts) {
        vm.counts = {
          studies: aggregateCounts.studies,
          centres: aggregateCounts.centres,
          users:   aggregateCounts.users
        };
      });
    }
  }

  return AdminCtrl;
});
