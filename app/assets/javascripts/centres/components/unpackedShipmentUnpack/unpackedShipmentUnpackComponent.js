/**
 * AngularJS Components used in {@link domain.centres.Shipment Shipping}
 *
 * @namespace centres.components.unpackedShipmentUnpack
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import { UnpackBaseController } from '../../controllers/UnpackBaseController';
import _ from 'lodash';

class UnpackedShipmentUnpackController extends UnpackBaseController {

  constructor($q,
              $scope,
              Shipment,
              ShipmentSpecimen,
              ShipmentItemState,
              gettextCatalog,
              modalService,
              notificationsService) {
    'ngInject';
    super(modalService, gettextCatalog);
    Object.assign(this,
                  {
                    $q,
                    $scope,
                    Shipment,
                    ShipmentSpecimen,
                    ShipmentItemState,
                    notificationsService
                  });
  }

  $onInit() {
    this.refreshTable = 0;
    this.actions =  [
      {
        id:    'tag-as-missing',
        title: this.gettextCatalog.getString('Tag as missing'),
        icon:  'glyphicon-cloud text-warning'
      }
    ];

    this.$scope.$emit('tabbed-page-update', 'tab-selected');
  }

  getPresentSpecimens(options) {
    if (!this.shipment) { return this.$q.when({ items: [], maxPages: 0 }); }

    options = options || {};
    Object.assign(options, { filter: 'state:in:' + this.ShipmentItemState.PRESENT });

    return this.ShipmentSpecimen.list(this.shipment.id, options)
      .then(paginatedResult => ({
        items:    paginatedResult.items,
        maxPages: paginatedResult.maxPages
      }));
  }

  /*
   * Inventory IDs entered by the user
   */
  onInventoryIdsSubmit() {
    if (!this.inventoryIds) {
      return null;
    }

    var inventoryIds = this.inventoryIds.split(',').map((nonTrimmedInventoryId) => nonTrimmedInventoryId.trim());
    return this.shipment.tagSpecimensAsReceived(inventoryIds)
      .then(() => {
        this.inventoryIds = '';
        this.refreshTable += 1;
        this.notificationsService.success(this.gettextCatalog.getString('Specimen(s) received'));
      })
      .catch(err => {
        let modalMsg;

        if (err.message) {
          modalMsg = this.errorIsShipSpecimensNotInShipment(err.message);

          if (modalMsg && (inventoryIds.length === 1)) {
            return this.checkIfTagAsExtra(inventoryIds[0]);
          }

          if (_.isUndefined(modalMsg)) {
            modalMsg = this.errorIsInvalidInventoryIds(err.message);
          }
          if (_.isUndefined(modalMsg)) {
            modalMsg = this.errorIsShipSpecimensNotPresent(err.message);
          }
        }

        if (modalMsg) {
          return this.modalService.modalOk(this.gettextCatalog.getString('Invalid inventory IDs'),
                                           modalMsg);
        }

        return this.modalService.modalOk(this.gettextCatalog.getString('Server error'),
                                         JSON.stringify(err));
      });
  }

  checkIfTagAsExtra(inventoryId) {
    return this.modalService.modalOkCancel(
      this.gettextCatalog.getString('Invalid inventory IDs'),
      this.gettextCatalog.getString(
        'Specimen with  inventory ID <b>{{inventoryId}}</b> is not in this shipment. Mark it as extra?',
        { inventoryId : inventoryId }))
      .then(() => {
        this.tagSpecimensAsExtra([ inventoryId ]);
      })
      .then(() => {
        this.notificationsService.success(this.gettextCatalog.getString('Specimen marked as extra'));
      });
  }

  tableActionSelected(shipmentSpecimen) {
    return this.shipment.tagSpecimensAsMissing([ shipmentSpecimen.specimen.inventoryId ])
      .then(() => {
        this.refreshTable += 1;
      });
  }

}

/**
 * An AngularJS component that allows user to interact with Shipment Specimens in {@link
 * domain.centres.ShipmentItemState PRESENT} state.
 *
 * The user can receive the specimens, or mark them as {@link domain.centres.ShipmentItemState EXTRA} or
 * {@link domain.centres.ShipmentItemState MISSING}.
 *
 * @memberOf centres.components.unpackedShipmentUnpack
 *
 * @param {domain.centres.Shipment} shipment - the shipment to display the items for.
 */
const unpackedShipmentUnpackComponent = {
  template: require('./unpackedShipmentUnpack.html'),
  controller: UnpackedShipmentUnpackController,
  controllerAs: 'vm',
  bindings: {
    shipment: '<'
  }
};

export default ngModule => ngModule.component('unpackedShipmentUnpack', unpackedShipmentUnpackComponent)
