define(['./module'], function(module) {
  'use strict';

  module.controller('AdminCtrl', AdminCtrl);

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

});
