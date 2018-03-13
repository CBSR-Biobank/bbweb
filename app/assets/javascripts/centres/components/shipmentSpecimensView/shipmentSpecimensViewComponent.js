/**
 * AngularJS Components used in {@link domain.centres.Shipment Shipping}
 *
 * @namespace centres.components.shipmentSpecimensView
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import { ShipmentSpecimensController } from '../../controllers/ShipmentSpecimensController'

class ShipmentSpecimensViewController extends ShipmentSpecimensController {

  constructor($q, ShipmentSpecimen) {
    'ngInject'
    super($q, ShipmentSpecimen)
  }

}

/**
 * An AngularJS component that lets the user to view the {@link domain.participants.Specimen Specimens} that
 * have been added to a {@link domain.centres.Shipment Shipment}.
 *
 * @memberOf centres.components.shipmentSpecimensView
 *
 * @param {domain.centres.Shipment} shipment - the shipment to display.
 *
 * @param {boolean} readOnly - when `FALSE` the user is allowed to make changes to the shipment.
 *
 * @param {boolean} showItemState - when `TRUE` the specimen's shipment state is also displayed.
 */
const shipmentSpecimensViewComponent = {
  template: require('./shipmentSpecimensView.html'),
  controller: ShipmentSpecimensViewController,
  controllerAs: 'vm',
  bindings: {
    shipment:      '<',
    readOnly:      '<',
    showItemState: '<'
  }
};

export default ngModule => ngModule.component('shipmentSpecimensView', shipmentSpecimensViewComponent)
