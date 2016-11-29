/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var _ = require('lodash');

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
    'gettextCatalog',
    'Centre'
  ];

  /**
   * Allows the user to select a centre associated to her account or the user group she is associated with.
   */
  function ShippingHomeController($state, gettextCatalog, Centre) {
    var vm = this;

    vm.updateCentres   = updateCentres;
    vm.hasValidCentres = false;
    vm.panelHeader     = gettextCatalog.getString('Select a centre to view its shipping information');
    vm.centreIcon      = 'glyphicon-ok-circle';
    vm.centreSelected  = centreSelected;

    vm.$onInit = onInit;

    //---

    function onInit() {
      return Centre.locationsSearch()
        .then(Centre.centreLocationToNames)
        .then(function (centreLocations) {
          vm.centreLocations = centreLocations;
          vm.hasValidCentres = (centreLocations.length > 1);
        });
    }

    function updateCentres(options) {
      var optCopy = _.extend({}, options);
      if (optCopy.filter !== '') {
        optCopy.filter += ';';
      }
      optCopy.filter += 'state::enabled';
      return Centre.list(optCopy);
    }

    function centreSelected(centre) {
      $state.go('home.shipping.centre', { centreId: centre.id });
    }
  }

  return component;
});
