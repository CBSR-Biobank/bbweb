/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var _ = require('lodash');

  /**
   *
   * @param {domain.centres.CentreLocationDto} - the locations returned from the server.
   */
  var component = {
    templateUrl : '/assets/javascripts/centres/components/shippingHome/shippingHome.html',
    controller: ShippingHomeController,
    controllerAs: 'vm',
    bindings: {
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

    vm.hasValidCentres = false;
    vm.centreIcon      = 'glyphicon-ok-circle';
    vm.updateCentres   = updateCentres;
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
