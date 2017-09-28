/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  ShippingComponentTestSuiteMixinFactory.$inject = [
    '$q',
    'ComponentTestSuiteMixin',
    'Shipment',
    'ShipmentSpecimen',
    'factory'];

  function ShippingComponentTestSuiteMixinFactory($q,
                                                  ComponentTestSuiteMixin,
                                                  Shipment,
                                                  ShipmentSpecimen,
                                                  factory) {

    function ShippingComponentTestSuiteMixin() {
      ComponentTestSuiteMixin.call(this);
    }

    ShippingComponentTestSuiteMixin.prototype = Object.create(ComponentTestSuiteMixin.prototype);
    ShippingComponentTestSuiteMixin.prototype.constructor = ShippingComponentTestSuiteMixin;

    ShippingComponentTestSuiteMixin.prototype.createShipment = function (state) {
      var options = {};
      if (state) {
        options.state = state;
      }
      return new Shipment(factory.shipment(options));
    };

    ShippingComponentTestSuiteMixin.prototype.createShipmentWithSpecimens = function (specimenCount) {
      return new Shipment(factory.shipment({ specimenCount: specimenCount }));
    };

    ShippingComponentTestSuiteMixin.prototype.createGetShipmentSpy = function (shipment) {
      spyOn(Shipment, 'get').and.returnValue($q.when(shipment));
    };

    ShippingComponentTestSuiteMixin.prototype.createShipmentSpecimensListSpy = function (shipmentSpecimens) {
      var reply = factory.pagedResult(shipmentSpecimens);
      spyOn(ShipmentSpecimen, 'list').and.returnValue($q.when(reply));
    };

    return ShippingComponentTestSuiteMixin;
  }

  return ShippingComponentTestSuiteMixinFactory;

});
