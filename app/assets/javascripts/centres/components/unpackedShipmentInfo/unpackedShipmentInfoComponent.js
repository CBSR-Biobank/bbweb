/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  var component = {
    template: '<shipping-info-view shipment="vm.shipment" read-only="true"></shipping-info-view>',
    controller: UnpackedShipmentInfoController,
    controllerAs: 'vm',
    bindings: {
      shipment: '<'
    }
  };

  UnpackedShipmentInfoController.$inject = [
    '$scope'
  ];

  /*
   * The controller for this component.
   */
  function UnpackedShipmentInfoController($scope) {
    var vm = this;
    vm.$onInit = onInit;

    //----

    function onInit() {
      $scope.$emit('tabbed-page-update', 'tab-selected');
    }
  }

  return component;
});
