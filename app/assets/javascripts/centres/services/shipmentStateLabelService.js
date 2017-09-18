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

    labels[ShipmentState.CREATED]   = function () { return gettextCatalog.getString('Created'); };
    labels[ShipmentState.PACKED]    = function () { return gettextCatalog.getString('Packed'); };
    labels[ShipmentState.SENT]      = function () { return gettextCatalog.getString('Sent'); };
    labels[ShipmentState.RECEIVED]  = function () { return gettextCatalog.getString('Received'); };
    labels[ShipmentState.UNPACKED]  = function () { return gettextCatalog.getString('Unpacked'); };
    labels[ShipmentState.COMPLETED] = function () { return gettextCatalog.getString('Completed'); };
    labels[ShipmentState.LOST]      = function () { return gettextCatalog.getString('Lost'); };

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
