/**
 * AngularJS Components used in {@link domain.centres.Shipment Shipping}
 *
 * @namespace centres.components.unpackedShipmentView
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import { TabbedPageController } from '../../../common/controllers/TabbedPageController';

/*
 * Controller for this component.
 */
class UnpackedShipmentViewController extends TabbedPageController {

  constructor($scope,
              $state,
              Shipment,
              ShipmentSpecimen,
              ShipmentItemState,
              modalService,
              modalInput,
              notificationsService,
              timeService,
              shipmentReceiveTasksService,
              gettextCatalog,
              breadcrumbService) {
    'ngInject';
    super(
      [
        {
          heading: gettextCatalog.getString('Information'),
          sref: 'home.shipping.shipment.unpack.info',
          active: true
        },{
          heading: gettextCatalog.getString('Unpack specimens'),
          sref: 'home.shipping.shipment.unpack.unpack',
          active: false
        },
        {
          heading: gettextCatalog.getString('Received specimens'),
          sref: 'home.shipping.shipment.unpack.received',
          active: false
        },
        {
          heading: gettextCatalog.getString('Missing specimens'),
          sref: 'home.shipping.shipment.unpack.missing',
          active: false
        },
        {
          heading: gettextCatalog.getString('Extra specimens'),
          sref: 'home.shipping.shipment.unpack.extra',
          active: false
        }
      ],
      0,
      $scope,
      $state);
    Object.assign(this,
                  {
                    Shipment,
                    ShipmentSpecimen,
                    ShipmentItemState,
                    modalService,
                    modalInput,
                    notificationsService,
                    timeService,
                    shipmentReceiveTasksService,
                    gettextCatalog,
                    breadcrumbService
                  });
  }

  $onInit() {
    this.breadcrumbs = [
      this.breadcrumbService.forState('home'),
      this.breadcrumbService.forState('home.shipping'),
      this.breadcrumbService.forStateWithFunc(
        'home.shipping.shipment.unpack.info',
        () => this.gettextCatalog.getString(
          'Unpack Shipment: {{courierName}} - {{trackingNumber}}',
          {
            courierName: this.shipment.courierName,
            trackingNumber: this.shipment.trackingNumber
          }))
    ];

    this.timeCompleted = new Date();

    this.progressInfo = this.shipmentReceiveTasksService.getTaskData().map((taskInfo, index) => {
      taskInfo.status = (index < 3);
      return taskInfo;
    });

    // get shipment again to get latest version
    return this.Shipment.get(this.shipment.id).then(shipment => {
      this.shipment = shipment;
    });
  }

  cannotGoBackToReceivedModal() {
    this.modalService.modalOk(
      this.gettextCatalog.getString('Cannot change state'),
      this.gettextCatalog.getString('Cannot return this shipment to <b>Received</b> state since ' +
                                    'specimens have already been unpacked.'));
  }

  backToReceived() {
    this.modalService.modalOkCancel(
      this.gettextCatalog.getString('Please confirm'),
      this.gettextCatalog.getString('Are you sure you want to place this shipment in <b>Received</b> state?'))
      .then(() => this.shipment.receive(this.shipment.timeReceived)
            .catch(err => this.notificationsService.updateError(err)))
      .then(() => {
        this.$state.go('home.shipping.shipment', { shipmentId: this.shipment.id }, { reload: true });
      });
  }

  returnToReceivedState() {
    this.ShipmentSpecimen.list(this.shipment.id,
                               { filter: 'state:out:' + this.ShipmentItemState.PRESENT })
      .then(pagedResult => {
        if (pagedResult.total > 0) {
          this.cannotGoBackToReceivedModal();
          return;
        }
        this.backToReceived();
      });
  }

  cannotCompleteShipmentModal() {
    this.modalService.modalOk(
      this.gettextCatalog.getString('Cannot change state'),
      this.gettextCatalog.getString('Cannot place this shipment in <b>Completed</b> state since ' +
                                    'it still has specimens that need unpacking.'));
  }

  completeShipmentConfirm() {
    this.modalInput
      .dateTime(this.gettextCatalog.getString('Date and time shipment was completed'),
                this.gettextCatalog.getString('Time completed'),
                this.timeCompleted,
                { required: true })
      .result
      .then(timeCompleted =>
            this.shipment.complete(this.timeService.dateAndTimeToUtcString(timeCompleted))
            .catch(error => this.notificationsService.updateError(error)))
      .then(() => {
        this.$state.go('home.shipping.shipment', { shipmentId: this.shipment.id }, { reload: true });
      });
  }

  completeShipment() {
    this.ShipmentSpecimen.list(this.shipment.id, { filter: 'state:in:' + this.ShipmentItemState.PRESENT })
      .then(pagedResult => {
        const hasPresentSpecimens = pagedResult.items.length > 0;

        if (hasPresentSpecimens) {
          this.cannotCompleteShipmentModal();
          return;
        }

        this.completeShipmentConfirm();
      });
  }

}

/**
 * An AngularJS component that displays information for a unpacked {@link domain.centres.Shipment Shipment}.
 *
 * @memberOf centres.components.unpackedShipmentView
 *
 * @param {domain.centres.Shipment} shipment - the shipment to display.
 */
const unpackedShipmentViewComponent = {
  template: require('./unpackedShipmentView.html'),
  controller: UnpackedShipmentViewController,
  controllerAs: 'vm',
  bindings: {
    shipment:  '<'
  }
};

export default ngModule => ngModule.component('unpackedShipmentView', unpackedShipmentViewComponent)
