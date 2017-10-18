/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */

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
/* @ngInject */
function shipmentStateLabelService(ShipmentState, gettextCatalog, labelService) {
  var labels = {};

  labels[ShipmentState.CREATED]   = () => gettextCatalog.getString('Created');
  labels[ShipmentState.PACKED]    = () => gettextCatalog.getString('Packed');
  labels[ShipmentState.SENT]      = () => gettextCatalog.getString('Sent');
  labels[ShipmentState.RECEIVED]  = () => gettextCatalog.getString('Received');
  labels[ShipmentState.UNPACKED]  = () => gettextCatalog.getString('Unpacked');
  labels[ShipmentState.COMPLETED] = () => gettextCatalog.getString('Completed');
  labels[ShipmentState.LOST]      = () => gettextCatalog.getString('Lost');

  var service = {
    stateToLabelFunc: stateToLabelFunc
  };
  return service;

  //-------

  function stateToLabelFunc(state) {
    return labelService.getLabel(labels, state);
  }

}

export default ngModule => ngModule.service('shipmentStateLabelService', shipmentStateLabelService)
