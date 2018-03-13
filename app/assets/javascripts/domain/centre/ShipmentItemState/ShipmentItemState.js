/**
 * Domain Entities related to {@link domain.centres.Centre Centres} and {@link domain.centres.Shipment
 * Shipping}.
 *
 * @namespace domain.centres.shipmentItemStates
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/**
 * The states that a {@link domain.centres.ShipmentSpecimen ShipmentSpecimen} can be in.
 *
 * @enum {string}
 *
 * @memberOf domain.centres.shipmentItemStates
 */
var ShipmentItemState = {
  /**
   * The item in the shipment is present in the physical package.
   */
  PRESENT: 'present',

  /**
   * The item in the shipment is was present in the physical package and removed.
   */
  RECEIVED: 'received',

  /**
   * The item in the shipment is was missing from the physical package.
   */
  MISSING: 'missing',

  /**
   * The physical package contained an item that was not in the manifest.
   */
  EXTRA: 'extra'
};

export default ngModule => ngModule.constant('ShipmentItemState', ShipmentItemState)
