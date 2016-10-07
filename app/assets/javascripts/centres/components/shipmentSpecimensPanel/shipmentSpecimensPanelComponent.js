/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  var component = {
    templateUrl : '/assets/javascripts/centres/components/shipmentSpecimensPanel/shipmentSpecimensPanel.html',
    controller: ShipmentSpecimensPanelController,
    controllerAs: 'vm',
    bindings: {
      heading:            '@',
      onGetSpecimens:     '&',
      noSpecimensMessage: '@',
      refresh:            '<'
    }
  };

  ShipmentSpecimensPanelController.$inject = [];

  /**
   * Used when unpacking specimens from a shipment.
   */
  function ShipmentSpecimensPanelController() {
    var vm = this;

    vm.panelOpen = true;

    vm.panelButtonClicked = panelButtonClicked;
    vm.getSpecimens = getSpecimens;

    //---

    function panelButtonClicked() {
      vm.panelOpen = ! vm.panelOpen;
    }

    function getSpecimens(options) {
      return vm.onGetSpecimens()(options);
    }
  }

  return component;
});
