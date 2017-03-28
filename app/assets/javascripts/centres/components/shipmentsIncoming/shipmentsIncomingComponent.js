define(function () {
  'use strict';

  /**
   *
   */
  var COMPONENT = {
    templateUrl: '/assets/javascripts/centres/components/shipmentsIncoming/shipmentsIncoming.html',
    controller: IncomingShipmentsController,
    controllerAs: 'vm',
    bindings: {
      centre: '<'
    }
  };

  IncomingShipmentsController.$inject = [
    '$scope',
    'Shipment',
    'ShipmentState',
    'SHIPMENT_TYPES'
  ];

  /*
   * Controller for this component.
   */
  function IncomingShipmentsController($scope,
                                       Shipment,
                                       ShipmentState,
                                       SHIPMENT_TYPES) {
    var vm = this;

    vm.shipmentTypes = SHIPMENT_TYPES.INCOMING;
    vm.$onInit = onInit;

    function onInit() {
      $scope.$emit('tabbed-page-update', 'tab-selected');
    }

  }

  return COMPONENT;
});
