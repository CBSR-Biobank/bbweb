/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */

var component = {
  template: require('./shipmentView.html'),
  controller: ShipmentViewController,
  controllerAs: 'vm',
  bindings: {
    shipment: '<'
  }
};

/* @ngInject */
function ShipmentViewController(gettextCatalog, ShipmentState, breadcrumbService) {
  var vm = this;
  vm.$onInit = onInit;

  //--

  function onInit() {
    vm.breadcrumbs = [
      breadcrumbService.forState('home'),
      breadcrumbService.forState('home.shipping'),
      breadcrumbService.forStateWithFunc('home.shipping.shipment', function () {
        return gettextCatalog.getString(
          'Shipment: {{courierName}} - {{trackingNumber}}',
          {
            courierName: vm.shipment.courierName,
            trackingNumber: vm.shipment.trackingNumber
          });

      })
    ];

    vm.pageHeader = getPageHeader();
    vm.shipmentStateValid = ((vm.shipment.state === ShipmentState.PACKED) ||
                             (vm.shipment.state === ShipmentState.SENT) ||
                             (vm.shipment.state === ShipmentState.RECEIVED) ||
                             (vm.shipment.state === ShipmentState.COMPLETED) ||
                             (vm.shipment.state === ShipmentState.LOST));
    vm.showSpecimenState = (vm.shipment.state === ShipmentState.COMPLETED);
  }

  function getPageHeader() {
    switch (vm.shipment.state) {
    case ShipmentState.PACKED:    return gettextCatalog.getString('Packed shipment');
    case ShipmentState.SENT:      return gettextCatalog.getString('Sent shipment');
    case ShipmentState.RECEIVED:  return gettextCatalog.getString('Received shipment');
    case ShipmentState.COMPLETED: return gettextCatalog.getString('Completed shipment');
    case ShipmentState.LOST:      return gettextCatalog.getString('Lost shipment');
    default:
      // page should not display this result
      return '';
    }
  }
}

export default ngModule => ngModule.component('shipmentView', component)
