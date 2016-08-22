/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(['lodash'], function (_) {
  'use strict';

  var component = {
    templateUrl : '/assets/javascripts/centres/components/shipmentAdd/shipmentAdd.html',
    controller: ShipmentAddController,
    controllerAs: 'vm',
    bindings: {}
  };

  ShipmentAddController.$inject = [
    '$state',
    'Centre',
    'Shipment',
    'domainEntityService',
    'notificationsService',
    'shipmentProgressItems'
  ];

  /**
   *
   */
  function ShipmentAddController($state,
                                 Centre,
                                 Shipment,
                                 domainEntityService,
                                 notificationsService,
                                 shipmentProgressItems) {
    var vm = this;

    vm.progressInfo = {
      items: shipmentProgressItems,
      current: 1
    };

    vm.centreLocationsByName    = [];
    vm.hasValidCentres          = false;
    vm.shipment                 = new Shipment();
    vm.selectedFromLocationInfo = undefined;
    vm.selectedToLocationInfo   = undefined;

    vm.$onInit               = onInit;
    vm.submit                = submit;
    vm.cancel                = cancel;
    vm.getCentreLocationInfo = getCentreLocationInfo;

    //--

    function onInit() {
      return Centre.locationsSearch('').then(function (results) {
        vm.centreLocationsByName = _.keyBy(results, 'name');
        vm.hasValidCentres = (results.length > 1);
      });
    }

    function submit(specimenSpec) {
      var fromLocation = vm.centreLocationsByName[vm.selectedFromLocationInfo],
          toLocation = vm.centreLocationsByName[vm.selectedToLocationInfo];

      if (!fromLocation) {
        throw new Error('could not determine from location: ' + vm.selectedFromLocationInfo);
      }

      if (!toLocation) {
        throw new Error('could not determine from location: ' + vm.selectedFromLocationInfo);
      }

      vm.shipment.fromLocationInfo = { locationId: fromLocation.locationId };
      vm.shipment.toLocationInfo = { locationId: toLocation.locationId };
      vm.shipment.add().then(onAddSuccessful).catch(onAddFailed);

      function onAddSuccessful(shipment) {
        $state.go('home.shipping.addItems', { shipmentId: shipment.id });
      }

      function onAddFailed(error) {
        domainEntityService.updateErrorModal(error, 'shipment');
      }
    }

    function cancel() {
      $state.go('home.shipping');
    }

    function getCentreLocationInfo(filter) {
      return Centre.locationsSearch(filter);
    }

  }

  return component;
});
