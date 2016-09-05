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
    CREATED:  'created',
    PACKED:   'packed',
    SENT:     'sent',
    RECEIVED: 'received',
    UNPACKED: 'unpacked',
    LOST:     'lost'
  };

  return ShipmentState;
});
