/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */

var component = {
  template: require('./shipmentViewLost.html'),
  controller: ShipmentViewLostController,
  controllerAs: 'vm',
  bindings: {
    shipment: '<'
  }
};

/*
 *
 */
/* @ngInject */
function ShipmentViewLostController($state,
                                    gettextCatalog,
                                    notificationsService,
                                    modalService) {
  var vm = this;
  vm.$onInit = onInit;

  //--

  function onInit() {
    vm.returnToSentState = returnToSentState;
  }

  function returnToSentState() {
    modalService.modalOkCancel(
      gettextCatalog.getString('Please confirm'),
      gettextCatalog.getString('Are you sure you return this shipment to <b>Sent</b> state?'))
      .then(function () {
        return vm.shipment.send(vm.shipment.timeSent)
          .catch(notificationsService.updateErrorAndReject);
      })
      .then(function () {
        $state.go('home.shipping.shipment', { shipmentId: vm.shipment.id }, { reload: true });
      });
  }
}

export default ngModule => ngModule.component('shipmentViewLost', component)
