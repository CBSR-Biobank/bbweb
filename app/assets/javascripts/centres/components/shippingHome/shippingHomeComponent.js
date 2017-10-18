/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash'

/**
 * Allows the user to select a centre associated to her account or the user group she is associated with.
 *
 * @param {domain.centres.CentreLocationDto} - the locations returned from the server.
 */
var component = {
  template: require('./shippingHome.html'),
  controller: ShippingHomeController,
  controllerAs: 'vm',
  bindings: {
  }
};

/* @ngInject */
function ShippingHomeController($state, gettextCatalog, Centre, breadcrumbService) {
  var vm = this;
  vm.$onInit = onInit;

  //---

  function onInit() {
    vm.hasValidCentres = false;
    vm.centreIcon      = 'glyphicon-ok-circle';
    vm.updateCentres   = updateCentres;
    vm.centreSelected  = centreSelected;

    vm.breadcrumbs = [
      breadcrumbService.forState('home'),
      breadcrumbService.forState('home.shipping')
    ];

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
    $state.go('home.shipping.centre.incoming', { centreId: centre.id });
  }
}

export default ngModule => ngModule.component('shippingHome', component)
