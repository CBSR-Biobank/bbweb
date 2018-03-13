/**
 * AngularJS Components used in {@link domain.centres.Shipment Shipping}
 *
 * @namespace centres.components.unpackedShipmentInfo
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/*
 * The controller for this component.
 */
/* @ngInject */
function UnpackedShipmentInfoController($scope) {
  var vm = this;
  vm.$onInit = onInit;

  //----

  function onInit() {
    $scope.$emit('tabbed-page-update', 'tab-selected');
  }
}

/**
 * An AngularJS component that lets the user view the information for an unpacked {@link
 * domain.centres.Shipment Shipment}.
 *
 * @memberOf centres.components.unpackedShipmentInfo
 *
 * @param {domain.centres.Shipment} shipment - the shipment to display information for.
 */
const unpackedShipmentInfoComponent = {
  template: '<shipping-info-view shipment="vm.shipment" read-only="true"></shipping-info-view>',
  controller: UnpackedShipmentInfoController,
  controllerAs: 'vm',
  bindings: {
    shipment: '<'
  }
};

export default ngModule => ngModule.component('unpackedShipmentInfo', unpackedShipmentInfoComponent)
