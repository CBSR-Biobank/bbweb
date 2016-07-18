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
    bindings: {
      centreLocations: '<'
    }
  };

  ShipmentAddController.$inject = [
    '$state',
    'Shipment',
    'domainEntityService',
    'notificationsService'
  ];

  /**
   *
   */
  function ShipmentAddController($state,
                                 Shipment,
                                 domainEntityService,
                                 notificationsService) {
    var vm = this;

    vm.hasValidCentres          = (vm.centreLocations.length > 1);
    vm.shipment                 = new Shipment();
    vm.fromCentreLocations      = _.clone(vm.centreLocations);
    vm.toCentreLocations        = _.clone(vm.centreLocations);
    vm.selectedFromLocationInfo = undefined;
    vm.selectedToLocationInfo   = undefined;

    vm.submit              = submit;
    vm.cancel              = cancel;
    vm.fromLocationChanged = fromLocationChanged;
    vm.toLocationChanged   = toLocationChanged;

    //--

    function submit(specimenSpec) {
      vm.shipment.fromLocationId = vm.selectedFromLocationInfo.locationId;
      vm.shipment.toLocationId = vm.selectedToLocationInfo.locationId;
      vm.shipment.add().then(onAddSuccessful).catch(onAddFailed);
    }

    function onAddSuccessful(shipment) {
      $state.go('home.shipping.addSpecimens', { shipmentId: shipment.id });
    }

    function onAddFailed(error) {
      domainEntityService.updateErrorModal(error, 'shipment');
    }

    function cancel() {
      $state.go('home.shipping');
    }

    function fromLocationChanged() {
      if (vm.selectedFromLocationInfo) {
        // remove selected location from options available for 'To centre'
        vm.toCentreLocations = _.omitBy(vm.centreLocations, function (item) {
          return item.locationId === vm.selectedFromLocationInfo.locationId;
        });
      } else {
        vm.toCentreLocations = _.clone(vm.centreLocations);
      }
    }

    function toLocationChanged() {
      if (vm.selectedToLocationInfo) {
        // remove selected location from options available for 'From centre'
        vm.fromCentreLocations = _.omitBy(vm.centreLocations, function (item) {
          return item.locationId === vm.selectedToLocationInfo.locationId;
        });
      } else {
        vm.fromCentreLocations = _.clone(vm.centreLocations);
      }
    }

  }

  return component;
});
