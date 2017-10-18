/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
/**
 *
 */
var COMPONENT = {
  template: require('./shipmentsIncoming.html'),
  controller: IncomingShipmentsController,
  controllerAs: 'vm',
  bindings: {
    centre: '<'
  }
};

/*
 * Controller for this component.
 */
/* @ngInject */
function IncomingShipmentsController($scope,
                                     Shipment,
                                     ShipmentState,
                                     SHIPMENT_TYPES) {
  var vm = this;
  vm.$onInit = onInit;

  function onInit() {
    vm.shipmentTypes = SHIPMENT_TYPES.INCOMING;
    $scope.$emit('tabbed-page-update', 'tab-selected');
  }

}

export default ngModule => ngModule.component('shipmentsIncoming', COMPONENT)
