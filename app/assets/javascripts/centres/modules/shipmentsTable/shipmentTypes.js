/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  /**
   * Used by componet "shipmentsTable" to specify the type of specimens it is displaying.
   */
  var SHIPMENT_TYPES = {
    INCOMING:  'incoming',
    OUTGOING:  'outgoing',
    COMPLETED: 'completed'
  };

  return SHIPMENT_TYPES;

});
