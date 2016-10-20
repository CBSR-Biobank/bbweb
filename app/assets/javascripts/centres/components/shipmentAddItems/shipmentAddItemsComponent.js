/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  //var _ = require('lodash');

  var component = {
    templateUrl : '/assets/javascripts/centres/components/shipmentAddItems/shipmentAddItems.html',
    controller: ShipmentAddItemsController,
    controllerAs: 'vm',
    bindings: {
      shipment: '<'
    }
  };

  ShipmentAddItemsController.$inject = [
    '$q',
    '$state',
    'gettextCatalog' ,
    'shipmentSendProgressItems',
    'Shipment',
    'ShipmentState',
    'modalInput',
    'modalService',
    'timeService',
    'notificationsService',
    'domainNotificationService',
    'shipmentSkipToSentModalService'
  ];

  /**
   * Allows the user to add items to a shipment.
   *
   * A task progress bar is used to give feedback to the user that this is one step in a multi-step process.
   */
  function ShipmentAddItemsController($q,
                                      $state,
                                      gettextCatalog,
                                      shipmentSendProgressItems,
                                      Shipment,
                                      ShipmentState,
                                      modalInput,
                                      modalService,
                                      timeService,
                                      notificationsService,
                                      domainNotificationService,
                                      shipmentSkipToSentModalService) {
    var vm = this;

    vm.tagAsPacked    = tagAsPacked;
    vm.tagAsSent      = tagAsSent;
    vm.removeShipment = removeShipment;

    vm.progressInfo = {
      items: shipmentSendProgressItems,
      current: 2
    };

    //--

    function validateStateChangeAllowed() {
      return Shipment.get(vm.shipment.id).then(function (shipment) {
        if (shipment.specimenCount <= 0) {
          modalService.modalOk(gettextCatalog.getString('Shipment has no specimens'),
                               gettextCatalog.getString('Please add specimens to this shipment fist.'));
          return $q.reject(false);
        }

        return $q.when(true);
      });
    }

    /**
     * Invoked by user when all items have been added to the shipment and it is now packed.
     */
    function tagAsPacked() {
      return validateStateChangeAllowed().then(function () {
        vm.timePacked = new Date();
        return modalInput.dateTime(gettextCatalog.getString('Date and time shipment was packed'),
                                   gettextCatalog.getString('Time packed'),
                                   vm.timePacked,
                                   { required: true }).result
          .then(function (timePacked) {
            return vm.shipment.changeState(ShipmentState.PACKED, timeService.dateAndTimeToUtcString(timePacked))
              .then(function (shipment) {
                return $state.go('home.shipping.shipment', { shipmentId: shipment.id});
              })
              .catch(notificationsService.updateError);
          });
      });
    }

    function tagAsSent() {
      return validateStateChangeAllowed().then(function () {
        vm.timePacked = new Date();
        vm.timeSent = new Date();
        return shipmentSkipToSentModalService.open().result
          .then(function (timeResult) {
            return vm.shipment.skipToStateSent(timeService.dateAndTimeToUtcString(timeResult.timePacked),
                                               timeService.dateAndTimeToUtcString(timeResult.timeSent))
              .then(function (shipment) {
                return $state.go('home.shipping.shipment', { shipmentId: shipment.id});
              })
              .catch(notificationsService.updateError);
          });
      });
    }

    function removeShipment() {
      if (!vm.shipment) { return; }

      domainNotificationService.removeEntity(
        doRemove,
        gettextCatalog.getString('Remove shipment'),
        gettextCatalog.getString('Are you sure you want to remove shipment {{trackingNumber}}?',
                                 { trackingNumber: vm.shipment.trackingNumber }),
        gettextCatalog.getString('Remove failed'),
        gettextCatalog.getString('Shipment {{trackingNumber}} cannot be removed',
                                 { trackingNumber: vm.shipment.trackingNumber }));

      function doRemove() {
        return vm.shipment.remove().then(function () {
          notificationsService.success(gettextCatalog.getString('Shipment removed'));
          $state.go('home.shipping');
        });
      }
    }
  }

  return component;
});
