/**
 * A component to display an entity's time added and time modified.
 */
define(function () {
   'use strict';

  var component = {
    templateUrl: '/assets/javascripts/common/components/entityTimestamps/entityTimestamps.html',
    controller: Controller,
    controllerAs: 'vm',
    bindings: {
      timeAdded:    '<',
      timeModified: '<'
    }
  };

  Controller.$inject = ['$controller'];

  /*
   *
   */
  function Controller($controller) {
    var vm = this;
    vm.$onInit = onInit;

    //--

    function onInit() {
      // initialize this controller's base class
      $controller('EntityTimestampsController', { vm: vm });
    }
  }

  return component;
});
