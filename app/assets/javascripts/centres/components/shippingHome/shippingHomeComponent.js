/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  var component = {
    templateUrl : '/assets/javascripts/centres/components/shippingHome/shippingHome.html',
    controller: ShippingHomeController,
    controllerAs: 'vm',
    bindings: {
      centreLocations: '<'
    }
  };

  ShippingHomeController.$inject = [
    '$state',
    'Centre'
  ];

  /**
   * Allows the user to select a centre associated to her account or the user group she is associated with.
   */
  function ShippingHomeController($state, Centre) {
    var vm = this;

    vm.updateCentres   = updateCentres;
    vm.hasValidCentres = (vm.centreLocations.length > 1);
    vm.panelHeader     = 'Select a centre to view its shipping information';
    vm.centreIcon      = 'glyphicon-ok-circle';
    vm.centreSelected  = centreSelected;

    //---

    function updateCentres(options) {
      return Centre.list(options);
    }

    function centreSelected(centre) {
      $state.go('home.shipping.centre', { centreId: centre.id });
    }
  }

  return component;
});
