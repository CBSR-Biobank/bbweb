/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */

var component = {
  template: '<shipping-info-view shipment="vm.shipment" read-only="true"></shipping-info-view>',
  controller: UnpackedShipmentInfoController,
  controllerAs: 'vm',
  bindings: {
    shipment: '<'
  }
};

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

export default ngModule => ngModule.component('unpackedShipmentInfo', component)
