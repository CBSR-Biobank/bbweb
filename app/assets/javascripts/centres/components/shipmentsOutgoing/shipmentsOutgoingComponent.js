define(function () {
  'use strict';

  /**
   *
   */
  var COMPONENT = {
    templateUrl: '/assets/javascripts/centres/components/shipmentsOutgoing/shipmentsOutgoing.html',
    controller: OutgoingShipmentsController,
    controllerAs: 'vm',
    bindings: {
      centre: '<'
    }
  };

  OutgoingShipmentsController.$inject = [
    '$scope',
    'Shipment',
    'ShipmentState',
    'SHIPMENT_TYPES'
  ];

  /*
   * Controller for this component.
   */
  function OutgoingShipmentsController($scope,
                                       Shipment,
                                       ShipmentState,
                                       SHIPMENT_TYPES) {
    var vm = this;

    vm.shipmentTypes = SHIPMENT_TYPES.OUTGOING;
    vm.$onInit = onInit;

    function onInit() {
      $scope.$emit('tabbed-page-update', 'tab-selected');
    }

  }

  return COMPONENT;
});
