/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  var component = {
    template: require('./debouncedTextInput.html'),
    controller: Controller,
    controllerAs: 'vm',
    bindings: {
      label:          '@',
      value:          '<',
      onValueChanged: '&'
    }
  };

  Controller.$inject = [];

  /*
   * Controller for this component.
   */
  function Controller() {
    var vm = this;
    vm.$onInit = onInit;
    vm.$onChanges = onChanges;

    function onInit() {
      vm.value = '';
      vm.updated = updated;
    }

    function onChanges(changed) {
      if (changed.value) {
        vm.value = changed.value.currentValue;
      }
    }

    function updated() {
      vm.onValueChanged()(vm.value);
    }
  }

  return component;
});
