/**
 * AngularJS Components used in {@link domain.centres.Shipment Shipping}
 *
 * @namespace centres.components.unpackedShipmentExtra
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import { UnpackBaseController } from '../../controllers/UnpackBaseController';
import angular from 'angular';

/*
 * Controller for this component.
 */
class UnpackedShipmentExtraController extends UnpackBaseController {

  constructor($q,
              $scope,
              ShipmentSpecimen,
              ShipmentItemState,
              domainNotificationService,
              notificationsService,
              modalService,
              gettextCatalog) {
    'ngInject';
    super(modalService, gettextCatalog);
    Object.assign(this,
                  {
                    $q,
                    $scope,
                    ShipmentSpecimen,
                    ShipmentItemState,
                    domainNotificationService,
                    notificationsService
                  });
  }

  $onInit() {
    this.refreshTable = 0;
    this.actions =  [
      {
        id:    'tag-as-extra',
        title: this.gettextCatalog.getString('Remove'),
        icon:  'glyphicon-remove text-danger'
      }
    ];
    this.$scope.$emit('tabbed-page-update', 'tab-selected');
  }

  getExtraSpecimens(options) {
    if (!this.shipment) { return this.$q.when({ items: [], maxPages: 0 }); }

    options = options || {};
    Object.assign(options, { filter: 'state:in:' + this.ShipmentItemState.EXTRA });

    return this.ShipmentSpecimen.list(this.shipment.id, options)
      .then(paginatedResult => ({
        items:    paginatedResult.items,
        maxPages: paginatedResult.maxPages
      }));
  }

  /*
   * User entered inventory IDs entered to be marked as extra in this shipment.
   */
  onInventoryIdsSubmit() {
    var inventoryIds = this.inventoryIds.split(',')
        .map((nonTrimmedInventoryId) => nonTrimmedInventoryId.trim());
    return this.tagSpecimensAsExtra(inventoryIds);
  }

  /*
   * User wishes to remove this shipment specimen from this shipment.
   */
  tableActionSelected(shipmentSpecimen) {
    const promiseFn = () =>
          shipmentSpecimen.remove()
          .then(() => {
            this.refreshTable += 1;
            this.notificationsService.success(
              this.gettextCatalog.getString('Specimen returnted to unpacked'));
          });

    this.domainNotificationService.removeEntity(
      promiseFn,
      this.gettextCatalog.getString('Remove extra specimen'),
      this.gettextCatalog.getString(
        'Are you sure you want to remove specimen with inventory ID <strong>{{id}}</strong> ' +
          'as an <i>Extra</i> specimen from this shipment?',
        { id: shipmentSpecimen.specimen.inventoryId }),
      this.gettextCatalog.getString('Remove failed'),
      this.gettextCatalog.getString(
        'Specimen with ID {{id}} cannot be removed',
        { id: shipmentSpecimen.specimen.inventoryId }))
      .catch(angular.noop);

  }

}

/**
 * An AngularJS component that shows the user the items tagged as {@link domain.centres.ShipmentItemState
 * EXTRA} in a {@link domain.centres.Shipment Shipment}.
 *
 * @memberOf centres.components.unpackedShipmentExtra
 *
 * @param {domain.centres.Shipment} shipment - the shipment to display the items for.
 */
const unpackedShipmentExtraComponent = {
  template: require('./unpackedShipmentExtra.html'),
  controller: UnpackedShipmentExtraController,
  controllerAs: 'vm',
  bindings: {
    shipment: '<'
  }
};

export default ngModule => ngModule.component('unpackedShipmentExtra', unpackedShipmentExtraComponent)
