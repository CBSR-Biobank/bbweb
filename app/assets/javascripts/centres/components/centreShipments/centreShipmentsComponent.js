/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  /**
   * Displays all shipments destined to or from a centre.
   */
  var component = {
    templateUrl : '/assets/javascripts/centres/components/centreShipments/centreShipments.html',
    controller: CentreShipmentsController,
    controllerAs: 'vm',
    bindings: {
      centre: '<'
    }
  };

  CentreShipmentsController.$inject = [
    'Shipment',
    'ShipmentState'
  ];

  /*
   * Controller for this component.
   */
  function CentreShipmentsController(Shipment, ShipmentState) {
    var vm = this;

    vm.hasShipments = false;
    vm.selectedShipmentStates = [
      ShipmentState.CREATED,
      ShipmentState.PACKED,
      ShipmentState.SENT
    ];
    vm.stateSelectionChanged = stateSelectionChanged;

    init();

    //--

    function init() {
      Shipment.list(vm.centre.id).then(function (result) {
        vm.hasShipments = (result.items.length > 0);
      });
    }

    function stateSelectionChanged(states) {
      vm.selectedShipmentStates = states;
    }
  }

  return component;
});
