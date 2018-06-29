/**
 * AngularJS component available to the rest of the application.
 *
 * @namespace common.components.dateTimePicker
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */


class DateTimePickerController {

  constructor(AppConfig) {
    'ngInject';
    Object.assign(this, { AppConfig });
  }

  $onInit() {
    this.value                = this.defaultValue;
    this.labelCols            = this.labelCols || 'col-sm-2';
    this.inputCols            = this.inputCols || 'col-sm-10';
    this.open                 = false;
    this.datetimePickerFormat = this.AppConfig.datepickerFormat;
    this.timepickerOptions    = { readonlyInput: false, showMeridian: false };
  }

  openCalendar() {
    this.open = true;
  }

  onChange() {
    this.onEdit()(this.value);
  }

}

/**
 * An AngularJS component that allows the user to pick a date and a time.
 *
 * @memberOf common.components.dateTimePicker
 *
 * @param {string} label - The label to display for the time picker.
 *
 * @param {Date} defaultValue - The initial value to display.
 *
 * @param {boolean} required - Set to `TRUE` if a value is required.
 *
 * @param {common.components.dateTimePicker.onEdit} onEdit -The function that is called after the user has
 * entered a new value.
 *
 * @param {int} labelCols - The number of Bootstrap columns for the label field.
 *
 * @param {int} inputCols - The number of Bootstrap columns for the input field.
 */
const dateTimePickerComponent = {
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
 * The callback function called by {@link common.components.dateTimePicker dateTimePicker} when the user
 * enters a new value.
 *
 * @callback common.components.dateTimePicker.onEdit
 *
 * @param {Date} newValue - the value entered by the user.
 */


export default ngModule => ngModule.component('dateTimePicker', dateTimePickerComponent)
