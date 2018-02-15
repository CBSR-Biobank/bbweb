/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */

/**
 * @class ng.admin.common.components.locationAdd
 *
 * An AngularJS component that allows the user to add an a {@link domain.Location Location} to a {@link
 * domain.centres.Centre Centre} using an HTML form.
 *
 * @memberOf ng.admin.common.components
 *
 * @param {ng.admin.common.components.locationAdd.onSubmit} onSubmit The function that is called when
 * the user submits the form.
 *
 * @param {ng.admin.common.components.locationAdd.onCancel} onCancel The function that is called when the
 * user presses the *Cancel* button on the form.
 */
const locationAdd = {
  template: require('./locationAdd.html'),
  controller: LocationAddController,
  controllerAs: 'vm',
  bindings: {
    onSubmit: '&',
    onCancel: '&'
  }
};

/**
 * The callback function called by component {@link ng.admin.common.components.locationAdd locationAdd} after
 * the user presses the *Submit* button in the component's HTML form.
 *
 * @callback ng.admin.common.components.locationAdd.onSubmit
 * @param {domain.Location} location - the location with the values entered by the user.
 */

/**
 * The callback function called by component {@link ng.admin.common.components.locationAdd locationAdd} after
 * the user presses the *Cancel* button in the component's HTML form.
 *
 * @callback ng.admin.common.components.locationAdd.onCancel
 */


/*
 * Controller for this component.
 */
/* @ngInject */
function LocationAddController(Location) {
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

export default ngModule => ngModule.component('locationAdd', locationAdd)
