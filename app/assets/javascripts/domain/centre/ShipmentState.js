/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  /**
   * @enum {string}
   * @memberOf domain.centres
   */
  var ShipmentState = {
    CREATED:  'Created',
    PACKED:   'Packed',
    SENT:     'Sent',
    RECEIVED: 'Received',
    UNPACKED: 'Unpacked',
    LOST:     'Lost'
  };


  return ShipmentState;
});
