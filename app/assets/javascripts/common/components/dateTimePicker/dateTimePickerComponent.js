/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */

/**
 * @class dateTimePicker
 *
 * An AngularJS component that allows the user to pick a date and a time.
 *
 * @memberOf ng.common.components
 *
 * @param {string} label - The label to display for the time picker.
 *
 * @param {Date} defaultValue - The initial value to display.
 *
 * @param {boolean} required - Set to *TRUE* if a value is required.
 *
 * @param {ng.common.components.dateTimePicker.onEdit} onEdit -The function that is called after the user has
 * entered a new value.
 *
 * @param {int} labelCols - The number of Bootstrap columns for the label field.
 *
 * @param {int} inputCols - The number of Bootstrap columns for the input field.
 */
const dateTimePicker = {
  template : require('./dateTimePicker.html'),
  controller: DateTimePickerController,
  controllerAs: 'vm',
  bindings: {
    label:        '@',
    defaultValue: '<',
    required:     '<',
    onEdit:       '&',
    labelCols:    '@',
    inputCols:    '@'
  }
};

/**
 * The callback function called by {@link ng.common.components.dateTimePicker dateTimePicker} when the user
 * enters a new value.
 *
 * @callback ng.common.components.dateTimePicker.onEdit
 *
 * @param {Date} newValue - the value entered by the uers.
 */


/* @ngInject */
function DateTimePickerController(AppConfig) {
  var vm = this;
  vm.$onInit = onInit;

  //--

  function onInit() {
    vm.value                = vm.defaultValue;
    vm.labelCols            = vm.labelCols || 'col-sm-2';
    vm.inputCols            = vm.inputCols || 'col-sm-10';
    vm.open                 = false;
    vm.openCalendar         = openCalendar;
    vm.datetimePickerFormat = AppConfig.datepickerFormat;
    vm.timepickerOptions    = { readonlyInput: false, showMeridian: false };
    vm.onChange             = onChange;
  }

  function openCalendar() {
    vm.open = true;
  }

  function onChange() {
    vm.onEdit()(vm.value);
  }
}

export default ngModule => ngModule.component('dateTimePicker', dateTimePicker)
