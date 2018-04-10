/**
 * Domain Entities related to {@link domain.centres.Centre Centres} and {@link domain.centres.Shipment
 * Shipping}.
 *
 * @namespace domain.centres.shipmentStates
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/**
 * The states that a {@link domain.centres.Shipment Shipment} can be in.
 *
 * @enum {string}
 *
 * @memberOf domain.centres.shipmentStates
 */
const ShipmentState = {
  /** A shipment that is being created. Items are still being added to it.*/
  CREATED:   'created',

  /** A shipment that has been packed. All items have been added to it. */
  PACKED:    'packed',

  /** A shipment that has been sent to it's destination. */
  SENT:      'sent',

  /** A shipment that has been received at it's destination. */
  RECEIVED:  'received',

  /** A shipment that is being unpacked. Items are being unpacked. */
  UNPACKED:  'unpacked',

  /** A shipment that who's items were unpacked and stored. */
  COMPLETED: 'completed',

  /** A shipment that was lost during transit. */
  LOST:      'lost'
};

export default ngModule => ngModule.constant('ShipmentState', ShipmentState)
