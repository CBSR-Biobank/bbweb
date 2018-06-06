/**
 * AngularJS Components used in {@link domain.centres.Shipment Shipping}
 *
 * @namespace centres.components.shipmentViewLost
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
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
      .then(() => vm.shipment.send(vm.shipment.timeSent))
      .catch(err => notificationsService.updateError(err))
      .then(() => {
        $state.go('home.shipping.shipment', { shipmentId: vm.shipment.id }, { reload: true });
      });
  }
}

/**
 * An AngularJS component that displays information for a lost {@link domain.centres.Shipment Shipment}.
 *
 * @memberOf centres.components.shipmentViewLost
 *
 * @param {domain.centres.Shipment} shipment - the shipment to display.
 */
const shipmentViewLostComponent = {
  template: require('./shipmentViewLost.html'),
  controller: ShipmentViewLostController,
  controllerAs: 'vm',
  bindings: {
    shipment: '<'
  }
};

export default ngModule => ngModule.component('shipmentViewLost', shipmentViewLostComponent)
