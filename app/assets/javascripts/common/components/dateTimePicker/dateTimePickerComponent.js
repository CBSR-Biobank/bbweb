/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  var component = {
    templateUrl : '/assets/javascripts/common/components/dateTimePicker/dateTimePicker.html',
    controller: DateTimePickerController,
    controllerAs: 'vm',
    bindings: {
      label: '@',
      defaultValue: '<',
      required: '<',
      onEdit: '&',
      labelCols: '@',
      inputCols: '@'
    }
  };

  DateTimePickerController.$inject = ['bbwebConfig'];

  /**
   *
   */
  function DateTimePickerController(bbwebConfig) {
    var vm = this;

    vm.value                = vm.defaultValue;
    vm.labelCols            = vm.labelCols || 'col-sm-2';
    vm.inputCols            = vm.inputCols || 'col-sm-10';
    vm.open                 = false;
    vm.openCalendar         = openCalendar;
    vm.datetimePickerFormat = bbwebConfig.datepickerFormat;
    vm.timepickerOptions    = { readonlyInput: false, showMeridian: false };
    vm.onChange             = onChange;

    //---

    function openCalendar(e) {
      vm.open = true;
    }

    function onChange() {
      vm.onEdit()(vm.value);
    }
  }

  return component;
});
