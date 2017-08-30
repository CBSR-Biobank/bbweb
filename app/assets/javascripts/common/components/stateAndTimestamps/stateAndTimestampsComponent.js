/**
 * A component to display an entity's time added and time modified.
 */
define(function () {
   'use strict';

  var component = {
    templateUrl: '/assets/javascripts/common/components/stateAndTimestamps/stateAndTimestamps.html',
    controller: Controller,
    controllerAs: 'vm',
    bindings: {
      stateLabelFunc: '&',
      timeAdded:      '<',
      timeModified:   '<'
    }
  };

  //Controller.$inject = [''];

  /*
   *
   */
  function Controller() {
  }

  return component;
});
