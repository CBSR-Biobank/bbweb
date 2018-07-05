/**
 * AngularJS Components used in {@link domain.centres.Shipment Shipping}
 *
 * @namespace centres.components.shipmentViewReceived
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/* @ngInject */
function ShipmentViewReceivedController($q,
                                        $state,
                                        gettextCatalog,
                                        modalInput,
                                        notificationsService,
                                        timeService,
                                        modalService,
                                        shipmentReceiveTasksService) {
  var vm = this;
  vm.$onInit = onInit;

  //--

  function onInit() {
    vm.timeUnpacked      = new Date();
    vm.unpackShipment    = unpackShipment;
    vm.returnToSentState = returnToSentState;

    vm.progressInfo = shipmentReceiveTasksService.getTaskData().map((taskInfo, index) => {
      taskInfo.status = (index < 2);
      return taskInfo;
    });
  }

  function unpackShipment() {
    modalInput.dateTime(gettextCatalog.getString('Date and time shipment was unpacked'),
                        gettextCatalog.getString('Time unpacked'),
                        vm.timeUnpacked,
                        { required: true }).result
      .then(timeUnpacked =>
            vm.shipment.unpack(timeService.dateAndTimeToUtcString(timeUnpacked))
            .catch(function (err) {
              if (err.message === 'TimeUnpackedBeforeReceived') {
                err.message = gettextCatalog.getString('The unpacked time is before the received time');
              }
            notificationsService.updateError(err);
            }))
      .then(function () {
        $state.go('home.shipping.shipment.unpack.info', { shipmentId: vm.shipment.id });
      });
  }

  function returnToSentState() {
    modalService.modalOkCancel(
      gettextCatalog.getString('Please confirm'),
      gettextCatalog.getString('Are you sure you want to place this shipment in <b>sent</b> state?'))
      .then(() => vm.shipment.send(vm.shipment.timeSent)
            .catch(error => {
              notificationsService.updateError(error);
            }))
      .then(() => {
        $state.reload();
      });
  }
}

/**
 * An AngularJS component that displays information for a received {@link domain.centres.Shipment Shipment}.
 *
 * @memberOf centres.components.shipmentViewReceived
 *
 * @param {domain.centres.Shipment} shipment - the shipment to display.
 */
const shipmentViewReceivedComponent = {
  template: require('./shipmentViewReceived.html'),
  controller: ShipmentViewReceivedController,
  controllerAs: 'vm',
  bindings: {
    shipment: '<'
  }
};

export default ngModule => ngModule.component('shipmentViewReceived', shipmentViewReceivedComponent)
