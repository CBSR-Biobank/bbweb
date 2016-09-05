/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var component = {
    templateUrl : '/assets/javascripts/centres/components/shipmentView/shipmentView.html',
    controller: ShipmentViewController,
    controllerAs: 'vm',
    bindings: {
      shipment: '<'
    }
  };

  ShipmentViewController.$inject = [
  ];

  /**
   *
   */
  function ShipmentViewController() {
  }

  return component;
});
