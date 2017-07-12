/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
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
    vm.$onInit = onInit;

    function onInit() {
      vm.shipmentTypes = SHIPMENT_TYPES.OUTGOING;
      $scope.$emit('tabbed-page-update', 'tab-selected');
    }

  }

  return COMPONENT;
});
