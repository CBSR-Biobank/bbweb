/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */

import { ShipmentSpecimensController } from '../../controllers/ShipmentSpecimensController'

/**
 * This controller subclasses {@link ShipmentSpecimensController}.
 */
class ShipmentSpecimensAddController extends ShipmentSpecimensController {

  constructor($q,
              $log,
              gettextCatalog,
              modalService,
              modalInput,
              domainNotificationService,
              notificationsService,
              Specimen,
              ShipmentSpecimen) {
    'ngInject'
    super($q, ShipmentSpecimen)

    Object.assign(this, {
      $log,
      gettextCatalog,
      modalService,
      modalInput,
      domainNotificationService,
      notificationsService,
      Specimen
    })
  }

  $onInit() {
    this.inventoryIds           = ''
    this.refreshSpecimensTable  = 0

    this.actions =  [{
      id:    'remove',
      class: 'btn-warning',
      title: this.gettextCatalog.getString('Remove specimen'),
      icon:  'glyphicon-remove'
    }]
  }

  addSpecimens() {
    if (!this.inventoryIds) {
      // nothing to do
      return
    }

    const inventoryIdsArr = this.inventoryIds.split(',')
    this.shipment.addSpecimens(inventoryIdsArr)
      .then(() => {
        this.notificationsService.success(this.gettextCatalog.getString('Specimens added'))
        this.updateSpecimensTable()
        this.inventoryIds = ''
      })
      .catch((error) => {
        var header = this.gettextCatalog.getString('Specimens cannot be added to shipment'),
            body,
            inventoryIds

        if (error && error.message) {
          inventoryIds = error.message.split(': ')

          if (error.message.match(/invalid specimen inventory IDs/)) {
            body = this.gettextCatalog.getString(
              'Inventory IDs not present in the system:<br><b>{{ids}}</b>',
              { ids: inventoryIds[2] })
          } else if (error.message.match(/specimens are already in an active shipment/)) {
            body = this.gettextCatalog.getString(
              'Inventory IDs already in this shipment or another shipment:<br><b>{{ids}}</b>',
              { ids: inventoryIds[2] })
          } else if (error.message.match(/invalid centre for specimen inventory IDs/)) {
            body = this.gettextCatalog.getString(
              'Inventory IDs at a different location than where shipment is coming from:<br><b>{{ids}}</b> ',
              { ids: inventoryIds[2] })
          } else {
            body = error.message
          }
        } else {
          body = JSON.stringify(error)
        }

        this.modalService.modalOk(header, body)
      })
  }

  updateSpecimensTable() {
    this.refreshSpecimensTable += 1
  }

  removeShipmentSpecimen(shipmentSpecimen) {
    var promiseFn = () => shipmentSpecimen.remove().then(() => {
      this.notificationsService.success(this.gettextCatalog.getString('Specimen removed'))
      this.updateSpecimensTable()
    })

    this.domainNotificationService.removeEntity(
      promiseFn,
      this.gettextCatalog.getString('Remove specimen'),
      this.gettextCatalog.getString(
        'Are you sure you want to remove specimen with inventory ID <strong>{{id}}</strong> ' +
          'from this shipment?',
        { id: shipmentSpecimen.specimen.inventoryId }),
      this.gettextCatalog.getString('Remove failed'),
      this.gettextCatalog.getString(
        'Specimen with ID {{id}} cannot be removed',
        { id: shipmentSpecimen.specimen.inventoryId }))
  }

}

const component = {
  template: require('./shipmentSpecimensAdd.html'),
  controller: ShipmentSpecimensAddController,
  controllerAs: 'vm',
  bindings: {
    shipment: '<',
    readOnly: '<'
  }
}

export default ngModule => ngModule.component('shipmentSpecimensAdd', component)
