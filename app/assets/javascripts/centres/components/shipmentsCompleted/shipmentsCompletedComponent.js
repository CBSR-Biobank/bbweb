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
    template: require('./shipmentsCompleted.html'),
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

    function onInit() {
      vm.shipmentTypes = SHIPMENT_TYPES.COMPLETED;
      $scope.$emit('tabbed-page-update', 'tab-selected');
    }

  }

  return COMPONENT;
});
