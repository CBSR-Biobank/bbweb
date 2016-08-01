/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  var component = {
    templateUrl : '/assets/javascripts/centres/components/centreShipments/centreShipments.html',
    controller: CentreShipmentsController,
    controllerAs: 'vm',
    bindings: {
      centre: '<'
    }
  };

  CentreShipmentsController.$inject = [
    'Shipment'
  ];

  /**
   * Displays all shipments destined to or from a centre.
   */
  function CentreShipmentsController(Shipment) {
    var vm = this;

    vm.hasShipments = false;

    init();

    //--

    function init() {
      Shipment.list(vm.centre.id).then(function (result) {
        vm.hasShipments = (result.items.length > 0);
      });
    }
  }

  return component;
});
