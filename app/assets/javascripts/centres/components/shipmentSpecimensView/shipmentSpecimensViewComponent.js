/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */

var component = {
  template: require('./shipmentSpecimensView.html'),
  controller: ShipmentSpecimensViewController,
  controllerAs: 'vm',
  bindings: {
    shipment:      '<',
    readOnly:      '<',
    showItemState: '<'
  }
};

/* @ngInject */
function ShipmentSpecimensViewController($q,
                                         $controller,
                                         ShipmentSpecimen) {
  var vm = this;
  vm.$onInit = onInit;

  //--

  function onInit() {
    $controller('ShipmentSpecimensController',
                {
                  vm:               vm,
                  $q:               $q,
                  ShipmentSpecimen: ShipmentSpecimen
                });
  }
}

export default ngModule => ngModule.component('shipmentSpecimensView', component)
