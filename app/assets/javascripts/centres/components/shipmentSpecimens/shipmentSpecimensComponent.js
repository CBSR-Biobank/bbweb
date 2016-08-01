/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  var component = {
    templateUrl : '/assets/javascripts/centres/components/shipmentSpecimens/shipmentSpecimens.html',
    controller: ShipmentSpecimensController,
    controllerAs: 'vm',
    bindings: {
      shipment: '<'
    }
  };

  ShipmentSpecimensController.$inject = [
  ];

  /**
   *
   */
  function ShipmentSpecimensController() {
    var vm = this;

    vm.panelOpen = true;

    vm.panelButtonClicked = panelButtonClicked;

    //---

    function panelButtonClicked() {
      vm.panelOpen = !vm.panelOpen;
    }


  }

  return component;
});
