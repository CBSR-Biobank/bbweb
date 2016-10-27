/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  var component = {
    templateUrl: '/assets/javascripts/centres/components/shipmentSpecimensPanel/shipmentSpecimensPanel.html',
    transclude: true,
    controller: ShipmentSpecimensPanelController,
    controllerAs: 'vm',
    bindings: {
      heading: '@'
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

    //---

    function panelButtonClicked() {
      vm.panelOpen = ! vm.panelOpen;
    }
  }

  return component;
});
