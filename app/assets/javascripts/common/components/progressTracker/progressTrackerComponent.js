/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  /*
   * A progress tracker that gives visual feedback to the user when a number of steps need to be completed in
   * a certain order.
   */
  var component = {
    template: require('./progressTracker.html'),
    controller: ProgressTrackerController,
    controllerAs: 'vm',
    bindings: {
      items: '<',
      current: '<'
    }
  };

  function ProgressTrackerController() {
    var vm = this;
    vm.$onInit = onInit;

    //--

    function onInit() {
      vm.numSteps = vm.items.length;
      vm.steps = vm.items.map((item, index) => ({
        name: item,
        class: (index < vm.current) ? 'progtrckr-done' : 'progtrckr-todo'
      }));
    }
  }

  return component;
});
