/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  /**
   * Used to add a loction to a Centre.
   *
   * @param {function} onSubmit is a function that takes a location as a parameter and returns a
   * promise (after accepting the new location).
   *
   * @param {function} onCancel is a function that is called when the user presses the Cancel button
   * when he / she no longer wants to add a location.
   */
  var component = {
    template: require('./locationAdd.html'),
    controller: LocationAddController,
    controllerAs: 'vm',
    bindings: {
        onSubmit: '&',
        onCancel: '&'
    }
  };

  LocationAddController.$inject = [ '$state', 'Location' ];

  /*
   * Controller for this component.
   */
  function LocationAddController($state, Location) {
    var vm = this;
    vm.$onInit = onInit;

    //--

    function onInit() {
      vm.location = new Location();
      vm.submit = submit;
      vm.cancel = cancel;
    }

    function submit(location) {
      vm.onSubmit()(location);
    }

    function cancel() {
      vm.onCancel()();
    }
  }

  return component;
});
