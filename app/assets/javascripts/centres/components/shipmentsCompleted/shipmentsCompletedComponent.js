define(function () {
  'use strict';

  /**
   *
   */
  var COMPONENT = {
    templateUrl: '/assets/javascripts/centres/components/shipmentsCompleted/shipmentsCompleted.html',
    controller: CompletedShipmentsController,
    controllerAs: 'vm',
    bindings: {
      centre: '<'
    }
  };

  CompletedShipmentsController.$inject = [
    '$scope',
    'Shipment',
    'ShipmentState',
    'SHIPMENT_TYPES'
  ];

  /*
   * Controller for this component.
   */
  function CompletedShipmentsController($scope,
                                        Shipment,
                                        ShipmentState,
                                        SHIPMENT_TYPES) {
    var vm = this;

    vm.$onInit = onInit;
    vm.shipmentTypes = SHIPMENT_TYPES.COMPLETED;

    function onInit() {
      $scope.$emit('tabbed-page-update', 'tab-selected');
    }

  }

  return COMPONENT;
});
