/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var _ = require('lodash');

  /*
   * A progress tracker that gives visual feedback to the user when a number of steps need to be completed in
   * a certain order.
   */
  var component = {
    templateUrl : '/assets/javascripts/common/components/progressTracker/progressTracker.html',
    controller: ProgressTrackerController,
    controllerAs: 'vm',
    bindings: {
      items: '<',
      current: '<'
    }
  };

  ProgressTrackerController.$inject = [];

  function ProgressTrackerController() {
    var vm = this;
    vm.$onInit = onInit;

    //--

    function onInit() {
      vm.numSteps = vm.items.length;
      vm.steps = _.map(vm.items, function (item, index) {
        return  {
          name: item,
          class: (index < vm.current) ? 'progtrckr-done' : 'progtrckr-todo'
        };
      });
    }
  }

  return component;
});
