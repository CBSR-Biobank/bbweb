/**
 * Administration module for adding {@link domain.Location Locations}.
 *
 * @namespace admin.common.components.biobankAdmin
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
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

/**
 * An AngularJS component that allows the user to add an a {@link domain.Location Location} to a {@link
 * domain.centres.Centre Centre} using an HTML form.
 *
 * @memberOf admin.common.components.locationAdd
 *
 * @param {admin.common.components.locationAdd.onSubmit} onSubmit The function that is called when
 * the user submits the form.
 *
 * @param {admin.common.components.locationAdd.onCancel} onCancel The function that is called when the
 * user presses the *Cancel* button on the form.
 */
const locationAddComponent = {
  template: require('./locationAdd.html'),
  controller: LocationAddController,
  controllerAs: 'vm',
  bindings: {
    onSubmit: '&',
    onCancel: '&'
  }
};

/**
 * The callback function called by {@link admin.common.components.locationAdd.locationAddComponent
 * locationAddComponent} after the user presses the *Submit* button in the component's HTML form.
 *
 * @callback admin.common.components.locationAdd.onSubmit
 * @param {domain.Location} location - the location with the values entered by the user.
 */

/**
 * The callback function called by {@link admin.common.components.locationAdd.locationAddComponent
 * locationAddComponent} after the user presses the *Cancel* button in the component's HTML form.
 *
 * @callback admin.common.components.locationAdd.onCancel
 */


export default ngModule => ngModule.component('locationAdd', locationAddComponent)
