/**
 * AngularJS component available to the rest of the application.
 *
 * @namespace common.components.debouncedTextInput
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/*
 * Controller for this component.
 */
function DebouncedTextInputController() {
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

/**
 * An AngularJS component that subclasses an *HTML Input Tag* and adds a *debounce* time that specifies how
 * often model updates are made to the bound `value` model object.
 *
 * This component can be used in an HTML Form.
 *
 * @memberOf common.components.debouncedTextInput
 *
 * @param {string} label - the label to display in the form for this input field.
 *
 * @param {string} value - the value to display in the input field.
 *
 * @param {common.components.debouncedTextInput.onValueChanged} onValueChanged - the function to call when the
 * value changes.
 */
const debouncedTextInputComponent = {
  template: require('./debouncedTextInput.html'),
  controller: DebouncedTextInputController,
  controllerAs: 'vm',
  bindings: {
    label:          '@',
    value:          '<',
    onValueChanged: '&'
  }
};

/**
 * The callback function called by {@link common.components.debouncedTextInput.debouncedTextInputComponent
 * debouncedTextInputComponent} when the value changes.
 *
 * @callback common.components.debouncedTextInput.onValueChanged
 *
 * @param {string} value - the value entered by the user.
 */

export default ngModule => ngModule.component('debouncedTextInput', debouncedTextInputComponent)
