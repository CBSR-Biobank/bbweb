/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import { ComponentTestSuiteMixin } from 'test/mixins/ComponentTestSuiteMixin';

/* @ngInject */
function ShippingComponentTestSuiteMixin($q,
                                         Shipment,
                                         ShipmentSpecimen,
                                         Factory) {

  return Object.assign(
    {
      createShipment,
      createShipmentWithSpecimens,
      createGetShipmentSpy,
      createShipmentSpecimensListSpy
    },
    ComponentTestSuiteMixin);

  function createShipment(options = {}) {
    return Shipment.create(Factory.shipment(options));
  }

  function createShipmentWithSpecimens(specimenCount) {
    return new Shipment(Factory.shipment({ specimenCount: specimenCount }));
  }

  function createGetShipmentSpy(shipment) {
    spyOn(Shipment, 'get').and.returnValue($q.when(shipment));
  }

  function createShipmentSpecimensListSpy(shipmentSpecimens) {
    var reply = Factory.pagedResult(shipmentSpecimens);
    spyOn(ShipmentSpecimen, 'list').and.returnValue($q.when(reply));
  }

}

export default ngModule => ngModule.service('ShippingComponentTestSuiteMixin', ShippingComponentTestSuiteMixin)
