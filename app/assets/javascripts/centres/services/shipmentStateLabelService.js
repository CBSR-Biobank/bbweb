/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  shipmentStateLabelService.$inject = [
    'ShipmentState',
    'gettextCatalog',
    'labelService'
  ];

  /**
   * An AngularJS service that converts a ShipmentState to a i18n string that can
   * be displayed to the user.
   *
   * @param {object} ShipmentState - AngularJS constant that enumerates all the shipment states.
   *
   * @param {object} gettextCatalog - The service that allows strings to be translated to other languages.
   *
   * @param {service} labelService - The service that validates the state and returns the label.
   *
   * @return {Service} The AngularJS service.
   */
  function shipmentStateLabelService(ShipmentState, gettextCatalog, labelService) {
    var labels = {};

    labels[ShipmentState.CREATED]   = function () { return gettextCatalog.getString('Created'); };
    labels[ShipmentState.PACKED]    = function () { return gettextCatalog.getString('Packed'); };
    labels[ShipmentState.SENT]      = function () { return gettextCatalog.getString('Sent'); };
    labels[ShipmentState.RECEIVED]  = function () { return gettextCatalog.getString('Received'); };
    labels[ShipmentState.UNPACKED]  = function () { return gettextCatalog.getString('Unpacked'); };
    labels[ShipmentState.COMPLETED] = function () { return gettextCatalog.getString('Completed'); };
    labels[ShipmentState.LOST]      = function () { return gettextCatalog.getString('Lost'); };

    var service = {
      stateToLabelFunc: stateToLabelFunc
    };
    return service;

    //-------

    function stateToLabelFunc(state) {
      return labelService.getLabel(labels, state);
    }

  }

  return shipmentStateLabelService;
});
