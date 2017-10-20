/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */

import { ShipmentSpecimensController } from '../../controllers/ShipmentSpecimensController'

class Controller extends ShipmentSpecimensController {

  constructor($q, ShipmentSpecimen) {
    'ngInject'
    super($q, ShipmentSpecimen)
  }

}

const component = {
  template: require('./shipmentSpecimensView.html'),
  controller: Controller,
  controllerAs: 'vm',
  bindings: {
    shipment:      '<',
    readOnly:      '<',
    showItemState: '<'
  }
};

export default ngModule => ngModule.component('shipmentSpecimensView', component)
