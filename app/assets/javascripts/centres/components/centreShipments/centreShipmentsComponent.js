/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */

/**
 * Displays all shipments destined to or from a centre.
 */
var component = {
  template: require('./centreShipments.html'),
  controller: CentreShipmentsController,
  controllerAs: 'vm',
  bindings: {
    centre: '<'
  }
};

/*
 * Controller for this component.
 */
/* @ngInject */
function CentreShipmentsController($controller,
                                   $scope,
                                   $state,
                                   Shipment,
                                   ShipmentState,
                                   gettextCatalog,
                                   breadcrumbService) {
  var vm = this;

  vm.$onInit = onInit;

  //--

  function onInit() {
    vm.breadcrumbs = [
      breadcrumbService.forState('home'),
      breadcrumbService.forState('home.shipping'),
      breadcrumbService.forStateWithFunc('home.shipping.centre', function () {
        return vm.centre.name;
      })
    ];

    // initialize this controller's base class
    $controller('TabbedPageController',
                {
                  vm:     vm,
                  $scope: $scope,
                  $state: $state
                });

    vm.tabs = [
      {
        heading: gettextCatalog.getString('Incoming'),
        sref: 'home.shipping.centre.incoming',
        active: true
      },
      {
        heading: gettextCatalog.getString('Outgoing'),
        sref: 'home.shipping.centre.outgoing',
        active: false
      },
      {
        heading: gettextCatalog.getString('Completed'),
        sref: 'home.shipping.centre.completed',
        active: false
      }
    ];

  }

}

export default ngModule => ngModule.component('centreShipments', component)
