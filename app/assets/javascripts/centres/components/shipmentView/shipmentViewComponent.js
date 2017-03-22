/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var component = {
    templateUrl : '/assets/javascripts/centres/components/shipmentView/shipmentView.html',
    controller: ShipmentViewController,
    controllerAs: 'vm',
    bindings: {
      shipment: '<'
    }
  };

  ShipmentViewController.$inject = ['gettextCatalog', 'ShipmentState'];

  function ShipmentViewController(gettextCatalog, ShipmentState) {
    var vm = this;

    vm.pageHeader = getPageHeader();

    vm.shipmentStateValid = ((vm.shipment.state === ShipmentState.PACKED) ||
                             (vm.shipment.state === ShipmentState.SENT) ||
                             (vm.shipment.state === ShipmentState.RECEIVED) ||
                             (vm.shipment.state === ShipmentState.COMPLETED));

    vm.showSpecimenState = (vm.shipment.state === ShipmentState.COMPLETED);

    //---

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

  return component;
});
