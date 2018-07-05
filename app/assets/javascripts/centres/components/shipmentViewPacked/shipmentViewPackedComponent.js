/**
 * AngularJS Components used in {@link domain.centres.Shipment Shipping}
 *
 * @namespace centres.components.shipmentViewPacked
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/* @ngInject */
function ShipmentViewPackedController($q,
                                      $state,
                                      gettextCatalog,
                                      shipmentSendTasksService,
                                      modalInput,
                                      notificationsService,
                                      timeService,
                                      modalService) {
  var vm = this;
  vm.$onInit = onInit;

  //--

  function onInit() {
    vm.timeSent     = new Date();
    vm.sendShipment = sendShipment;
    vm.addMoreItems = addMoreItems;

    vm.progressInfo = shipmentSendTasksService.getTaskData().map((taskInfo) => {
      taskInfo.status = true;
      return taskInfo;
    });
  }

  function sendShipment() {
    modalInput.dateTime(gettextCatalog.getString('Date and time shipment was sent'),
                        gettextCatalog.getString('Time sent'),
                        vm.timeSent,
                        { required: true }).result
      .then(function (timeSent) {
        return vm.shipment.send(timeService.dateAndTimeToUtcString(timeSent))
          .then(function () {
            $state.go('home.shipping');
          })
          .catch(function (err) {
            if (err.message === 'TimeSentBeforePacked') {
              err.message = gettextCatalog.getString('The sent time is before the packed time');
            }
            notificationsService.updateError(err);
          });
      });
  }

  function addMoreItems() {
    modalService.modalOkCancel(
      gettextCatalog.getString('Please confirm'),
      gettextCatalog.getString('Are you sure you want to add more items to this shipment?'))
      .then(() => vm.shipment.created())
      .then(() => {
        $state.go('home.shipping.addItems', { shipmentId: vm.shipment.id });
      })
      .catch(error => {
        notificationsService.updateError(error);
      });
  }
}

/**
 * An AngularJS component that displays information for a packed {@link domain.centres.Shipment Shipment}.
 *
 * @memberOf centres.components.shipmentViewPacked
 *
 * @param {domain.centres.Shipment} shipment - the shipment to display.
 */
const shipmentViewPackedComponent = {
  template: require('./shipmentViewPacked.html'),
  controller: ShipmentViewPackedController,
  controllerAs: 'vm',
  bindings: {
    shipment: '<'
  }
};

export default ngModule => ngModule.component('shipmentViewPacked', shipmentViewPackedComponent)
