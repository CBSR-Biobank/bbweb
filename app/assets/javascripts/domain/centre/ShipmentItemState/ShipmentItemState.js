/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */

/**
 * @enum {string}
 * @memberOf domain.centres
 */
var ShipmentItemState = {
  PRESENT:  'present',
  RECEIVED: 'received',
  MISSING:  'missing',
  EXTRA:    'extra'
};

export default ngModule => ngModule.constant('ShipmentItemState', ShipmentItemState)
