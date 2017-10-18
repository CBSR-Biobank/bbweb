/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */

var component = {
  template: require('./shipmentViewCompleted.html'),
  controller: ShipmentViewCompletedController,
  controllerAs: 'vm',
  bindings: {
    shipment: '<'
  }
};

/* @ngInject */
function ShipmentViewCompletedController($state,
                                         gettextCatalog,
                                         notificationsService,
                                         modalService,
                                         SHIPMENT_RECEIVE_PROGRESS_ITEMS) {
  var vm = this;
  vm.$onInit = onInit;

  //--

  function onInit() {
    vm.returnToUnpackedState = returnToUnpackedState;

    vm.progressInfo = {
      items: SHIPMENT_RECEIVE_PROGRESS_ITEMS,
      current: 4
    };
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

export default ngModule => ngModule.component('shipmentViewCompleted', component)
