/**
 * AngularJS Components used in {@link domain.centres.Shipment Shipping}
 *
 * @namespace centres.components.shippingHome
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash'

/* @ngInject */
function ShippingHomeController($state,
                                gettextCatalog,
                                Centre,
                                breadcrumbService,
                                resourceErrorService) {
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
      .catch(resourceErrorService.checkUnauthorized())
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
    $state.go('home.shipping.centre.incoming', { centreSlug: centre.slug });
  }
}

/**
 * An AngularJS component that displays the home page for {@link domain.centres.Shipment Shipping}.
 *
 * @memberOf centres.components.shippingHome
 */
const shippingHomeComponent = {
  template: require('./shippingHome.html'),
  controller: ShippingHomeController,
  controllerAs: 'vm',
  bindings: {
  }
};

export default ngModule => ngModule.component('shippingHome', shippingHomeComponent)
