/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */

/**
 * @enum {string}
 * @memberOf domain.centres
 */
const ShipmentState = {
  CREATED:   'created',
  PACKED:    'packed',
  SENT:      'sent',
  RECEIVED:  'received',
  UNPACKED:  'unpacked',
  COMPLETED: 'completed',
  LOST:      'lost'
};

export default ngModule => ngModule.constant('ShipmentState', ShipmentState)
