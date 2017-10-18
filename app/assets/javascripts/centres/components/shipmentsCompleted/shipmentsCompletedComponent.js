/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */

/**
 *
 */
var COMPONENT = {
  template: require('./shipmentsCompleted.html'),
  controller: CompletedShipmentsController,
  controllerAs: 'vm',
  bindings: {
    centre: '<'
  }
};

/*
 * Controller for this component.
 */
/* @ngInject */
function CompletedShipmentsController($scope,
                                      Shipment,
                                      ShipmentState,
                                      SHIPMENT_TYPES) {
  var vm = this;
  vm.$onInit = onInit;

  function onInit() {
    vm.shipmentTypes = SHIPMENT_TYPES.COMPLETED;
    $scope.$emit('tabbed-page-update', 'tab-selected');
  }

}

export default ngModule => ngModule.component('shipmentsCompleted', COMPONENT)
