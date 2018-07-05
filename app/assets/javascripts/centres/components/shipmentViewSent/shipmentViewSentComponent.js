/**
 * AngularJS Components used in {@link domain.centres.Shipment Shipping}
 *
 * @namespace centres.components.shipmentViewSent
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import angular from 'angular';

/*
 * Controller for this component.
 */
/* @ngInject */
function ShipmentViewSentController($q,
                                    $state,
                                    gettextCatalog,
                                    modalInput,
                                    notificationsService,
                                    timeService,
                                    modalService,
                                    shipmentReceiveTasksService,
                                    shipmentSkipToUnpackedModalService) {
  var vm = this;
  vm.$onInit = onInit;

  //--

  function onInit() {
    vm.timeReceived        = new Date();
    vm.returnToPackedState = returnToPackedState;
    vm.tagAsLost           = tagAsLost;
    vm.receiveShipment     = receiveShipment;
    vm.unpackShipment      = unpackShipment;

    vm.progressInfo = shipmentReceiveTasksService.getTaskData().map((taskInfo, index) => {
      taskInfo.status = (index < 1);
      return taskInfo;
    });
  }

  function returnToPackedState() {
    modalService.modalOkCancel(
      gettextCatalog.getString('Please confirm'),
      gettextCatalog.getString('Are you sure you want to place this shipment in <b>Packed</b> state?'))
      .then(() =>
            vm.shipment.pack(vm.shipment.timePacked)
            .catch(error => {
              notificationsService.updateError(error);
            }))
      .then(() => {
        $state.reload();
      });
  }

  function tagAsLost() {
    modalService.modalOkCancel(
      gettextCatalog.getString('Please confirm'),
      gettextCatalog.getString('Are you sure you want to tag this shipment as <b>Lost</b>?'))
      .then(() =>
            vm.shipment.lost()
            .catch(error => {
              notificationsService.updateError(error);
            }))
      .then(() => {
        $state.reload();
      });
  }

  function receiveShipment() {
    modalInput.dateTime(gettextCatalog.getString('Date and time shipment was received'),
                        gettextCatalog.getString('Time received'),
                        vm.timeReceived,
                        { required: true }).result
      .then(timeReceived =>
            vm.shipment.receive(timeService.dateAndTimeToUtcString(timeReceived))
            .catch(function (err) {
              if (err.message === 'TimeReceivedBeforeSent') {
                err.message = gettextCatalog.getString('The received time is before the sent time');
              }
            notificationsService.updateError(err);
            }))
      .then(function () {
        $state.reload();
      });
  }

  function unpackShipment() {
    return shipmentSkipToUnpackedModalService.open().result
      .then(timeResult =>
            vm.shipment.skipToStateUnpacked(timeService.dateAndTimeToUtcString(timeResult.timeReceived),
                                            timeService.dateAndTimeToUtcString(timeResult.timeUnpacked))
            .catch(function (err) {
              const newErr = {};
              if (err.message === 'TimeReceivedBeforeSent') {
                newErr.message =
                  gettextCatalog.getString('The received time is before the sent time');
              } else if (err.message === 'TimeUnpackedBeforeReceived') {
                newErr.message =
                  gettextCatalog.getString('The unpacked time is before the received time');
              }
              notificationsService.updateError(newErr);
              return $q.reject(newErr);
            }))
      .then(shipment => {
        $state.go('home.shipping.shipment.unpack.info', { shipmentId: shipment.id});
      })
      .catch(angular.noop);
  }
}

/**
 * An AngularJS component that displays information for a sent {@link domain.centres.Shipment Shipment}.
 *
 * @memberOf centres.components.shipmentViewSent
 *
 * @param {domain.centres.Shipment} shipment - the shipment to display.
 */
const shipmentViewSentComponent = {
  template: require('./shipmentViewSent.html'),
  controller: ShipmentViewSentController,
  controllerAs: 'vm',
  bindings: {
    shipment: '<'
  }
};

export default ngModule => ngModule.component('shipmentViewSent', shipmentViewSentComponent)
