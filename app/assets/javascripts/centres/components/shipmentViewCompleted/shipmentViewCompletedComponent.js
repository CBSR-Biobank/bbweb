/**
 * AngularJS Components used in {@link domain.centres.Shipment Shipping}
 *
 * @namespace centres.components.shipmentViewCompleted
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/* @ngInject */
function ShipmentViewCompletedController($state,
                                         gettextCatalog,
                                         notificationsService,
                                         modalService,
                                         shipmentReceiveTasksService) {
  var vm = this;
  vm.$onInit = onInit;

  //--

  function onInit() {
    vm.returnToUnpackedState = returnToUnpackedState;

    vm.progressInfo = shipmentReceiveTasksService.getTaskData().map(taskInfo => {
      taskInfo.status = true;
      return taskInfo;
    });
  }

  function returnToUnpackedState() {
    modalService.modalOkCancel(
      gettextCatalog.getString('Please confirm'),
      gettextCatalog.getString('Are you sure you want to place this shipment in <b>unpacked</b> state?'))
      .then(function () {
        return vm.shipment.unpack(vm.shipment.timeUnpacked)
          .then(function () {
            $state.go('home.shipping.shipment.unpack.info', { shipmentId: vm.shipment.id});
          })
          .catch(notificationsService.updateError);
      });
  }
}

/**
 * An AngularJS component that displays information for a completed {@link domain.centres.Shipment Shipment}.
 *
 * @memberOf centres.components.shipmentViewCompleted
 *
 * @param {domain.centres.Shipment} shipment - the shipment to display.
 */
const shipmentViewCompletedComponent = {
  template: require('./shipmentViewCompleted.html'),
  controller: ShipmentViewCompletedController,
  controllerAs: 'vm',
  bindings: {
    shipment: '<'
  }
};

export default ngModule => ngModule.component('shipmentViewCompleted', shipmentViewCompletedComponent)
