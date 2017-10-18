/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */

/**
 * Used by componet "shipmentsTable" to specify the type of specimens it is displaying.
 */
const SHIPMENT_TYPES = {
  INCOMING:  'incoming',
  OUTGOING:  'outgoing',
  COMPLETED: 'completed'
};

export default ngModule => ngModule.constant('SHIPMENT_TYPES', SHIPMENT_TYPES)
