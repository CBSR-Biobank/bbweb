/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define([], function() {
  'use strict';

  AdminCtrl.$inject = ['aggregateCounts'];

  /**
   * Administration controllers.
   *
   */
  function AdminCtrl(aggregateCounts) {
    var vm = this;

    vm.counts = {
      studies: aggregateCounts.studies,
      centres: aggregateCounts.centres,
      users:   aggregateCounts.users
    };
  }

  return AdminCtrl;
});
