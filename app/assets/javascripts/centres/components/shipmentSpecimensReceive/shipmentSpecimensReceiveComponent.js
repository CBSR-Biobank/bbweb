/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  var component = {
    templateUrl : '/assets/javascripts/centres/components/shipmentSpecimensReceive/shipmentSpecimensReceive.html',
    controller: ShipmentSpecimensReceiveController,
    controllerAs: 'vm',
    bindings: {
      shipment: '<'
    }
  };

  //ShipmentSpecimensReceiveController.$inject = [];

  /**
   * Used when unpacking specimens from a shipment.
   */
  function ShipmentSpecimensReceiveController() {

  }

  return component;
});
