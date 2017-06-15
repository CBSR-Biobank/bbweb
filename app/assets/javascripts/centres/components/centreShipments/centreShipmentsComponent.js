/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  /**
   * Displays all shipments destined to or from a centre.
   */
  var component = {
    templateUrl : '/assets/javascripts/centres/components/centreShipments/centreShipments.html',
    controller: CentreShipmentsController,
    controllerAs: 'vm',
    bindings: {
      centre: '<'
    }
  };

  CentreShipmentsController.$inject = [
    '$controller',
    '$scope',
    '$state',
    'Shipment',
    'ShipmentState',
    'gettextCatalog',
    'breadcrumbService'
  ];

  /*
   * Controller for this component.
   */
  function CentreShipmentsController($controller,
                                     $scope,
                                     $state,
                                     Shipment,
                                     ShipmentState,
                                     gettextCatalog,
                                     breadcrumbService) {
    var vm = this;

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

    //--

  }

  return component;
});
