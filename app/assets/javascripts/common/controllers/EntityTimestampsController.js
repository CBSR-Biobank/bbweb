/**
 *
 */
define(function () {
  'use strict';

  EntityTimestampsController.$inject = ['vm'];

  /**
   * Controller for components that display entity timestamp information
   */
  function EntityTimestampsController(vm) {
    if (vm.timeAdded.getFullYear() < 2000) {
      vm.timeAdded = undefined;
    }
  }

  return EntityTimestampsController;
});
