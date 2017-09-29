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
    template: require('./shipmentsIncoming.html'),
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
    vm.$onInit = onInit;

    function onInit() {
      vm.shipmentTypes = SHIPMENT_TYPES.INCOMING;
      $scope.$emit('tabbed-page-update', 'tab-selected');
    }

  }

  return COMPONENT;
});
