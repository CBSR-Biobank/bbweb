/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(['lodash'], function (_) {
  'use strict';

  var component = {
    templateUrl : '/assets/javascripts/centres/components/shipmentAddItems/shipmentAddItems.html',
    controller: ShipmentAddItemsController,
    controllerAs: 'vm',
    bindings: {
      shipmentId: '<'
    }
  };

  ShipmentAddItemsController.$inject = [
    '$state',
    'shipmentProgressItems',
    'Shipment',
    'modalInput',
    'modalService',
    'timeService',
    'notificationsService'
  ];

  /**
   *
   */
  function ShipmentAddItemsController($state,
                                      shipmentProgressItems,
                                      Shipment,
                                      modalInput,
                                      modalService,
                                      timeService,
                                      notificationsService) {
    var vm = this;

    vm.$onInit = onInit;
    vm.shipment = null;
    vm.allItemsAdded = allItemsAdded;

    vm.progressInfo = {
      items: shipmentProgressItems,
      current: 2
    };

    //--

    function onInit() {
      Shipment.get(vm.shipmentId).then(function (shipment){
        vm.shipment = shipment;
      });
    }

    /**
     * Invoked by user when all items have been added to the shipment and it is now packed.
     */
    function allItemsAdded() {
      Shipment.get(vm.shipment.id).then(function (shipment) {
        if (shipment.specimenCount > 0) {
          if (_.isUndefined(vm.timePacked)) {
            vm.timePacked = new Date();
          }
          return modalInput.dateTime('Date and time shipment was packed',
                                     'Time packed',
                                     vm.timePacked,
                                     { required: true }).result
            .then(function (timePacked) {
              return vm.shipment.packed(timeService.dateToUtcString(timePacked))
                .then(function (shipment) {
                  return $state.go('home.shipping.shipment', { shipmentId: shipment.id});
                })
                .catch(notificationsService.updateError);
            });
        }

        return modalService.modalOk('Shipment has no specimens',
                                    'Please add specimens to this shipment fist.');
      });
    }
  }

  return component;
});
