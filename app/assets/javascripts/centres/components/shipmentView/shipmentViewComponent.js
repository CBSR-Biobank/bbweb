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

  /**
   *
   */
  function ShipmentViewController(gettextCatalog, ShipmentState) {
    var vm = this;

    vm.pageHeader = getPageHeader();

    //---

    function getPageHeader() {
      switch (vm.shipment.state) {
      case ShipmentState.PACKED:   return gettextCatalog.getString('Packed shipment');
      case ShipmentState.SENT:     return gettextCatalog.getString('Sent shipment');
      case ShipmentState.RECEIVED: return gettextCatalog.getString('Received shipment');
      case ShipmentState.LOST:     return gettextCatalog.getString('Lost shipment');
      default:
        // page should not display this result
        return '';
      }
    }
  }

  return component;
});
