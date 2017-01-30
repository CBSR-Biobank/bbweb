/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var _ = require('lodash');

  shipmentStateLabelService.$inject = [
    'ShipmentState',
    'gettextCatalog'
  ];

  // /home/nelson/src/cbsr/scala/bbweb/app/assets/javascripts/centres

  /**
   * An AngularJS service that converts a ShipmentState to a i18n string that can
   * be displayed to the user.
   *
   * @param {object} ShipmentState - AngularJS constant that enumerates all the shipment states.
   *
   * @param {object} gettextCatalog - The service that allows strings to be translated to other languages.
   *
   * @return {Service} The AngularJS service.
   */
  function shipmentStateLabelService(ShipmentState, gettextCatalog) {
    var labels = {};

    labels[ShipmentState.CREATED]  = gettextCatalog.getString('Created');
    labels[ShipmentState.PACKED]   = gettextCatalog.getString('Packed');
    labels[ShipmentState.SENT]     = gettextCatalog.getString('Sent');
    labels[ShipmentState.RECEIVED] = gettextCatalog.getString('Received');
    labels[ShipmentState.UNPACKED] = gettextCatalog.getString('Unpacked');
    labels[ShipmentState.LOST]     = gettextCatalog.getString('Lost');

    var service = {
      stateToLabel: stateToLabel
    };
    return service;

    //-------

    function stateToLabel(state) {
      var result = labels[state];
      if (_.isUndefined(result)) {
        throw new Error('invalid shipment state: ' + state);
      }
      return result;
    }

  }

  return shipmentStateLabelService;
});
