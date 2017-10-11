/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash';

/* @ngInject */
export default function ShippingComponentTestSuiteMixinFactory($q,
                                                               ComponentTestSuiteMixin,
                                                               Shipment,
                                                               ShipmentSpecimen,
                                                               factory) {


  return _.extend(
    {
      createShipment: createShipment,
      createShipmentWithSpecimens: createShipmentWithSpecimens,
      createGetShipmentSpy: createGetShipmentSpy,
      createShipmentSpecimensListSpy: createShipmentSpecimensListSpy
    },
    ComponentTestSuiteMixin);

  function createShipment(state) {
    var options = {};
    if (state) {
      options.state = state;
    }
    return new Shipment(factory.shipment(options));
  }

  function createShipmentWithSpecimens(specimenCount) {
    return new Shipment(factory.shipment({ specimenCount: specimenCount }));
  }

  function createGetShipmentSpy(shipment) {
    spyOn(Shipment, 'get').and.returnValue($q.when(shipment));
  }

  function createShipmentSpecimensListSpy(shipmentSpecimens) {
    var reply = factory.pagedResult(shipmentSpecimens);
    spyOn(ShipmentSpecimen, 'list').and.returnValue($q.when(reply));
  }

}
