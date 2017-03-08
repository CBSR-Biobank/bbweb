/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var component = {
    templateUrl : '/assets/javascripts/centres/components/shipmentViewPacked/shipmentViewPacked.html',
    controller: ShipmentViewPackedController,
    controllerAs: 'vm',
    bindings: {
      shipment: '<'
    }
  };

  ShipmentViewPackedController.$inject = [
    '$q',
    '$state',
    'gettextCatalog',
    'SHIPMENT_SEND_PROGRESS_ITEMS',
    'modalInput',
    'notificationsService',
    'timeService',
    'modalService'
  ];

  /**
   *
   */
  function ShipmentViewPackedController($q,
                                        $state,
                                        gettextCatalog,
                                        SHIPMENT_SEND_PROGRESS_ITEMS,
                                        modalInput,
                                        notificationsService,
                                        timeService,
                                        modalService) {
    var vm = this;

    vm.sendShipment = sendShipment;
    vm.addMoreItems = addMoreItems;

    vm.progressInfo = {
      items: SHIPMENT_SEND_PROGRESS_ITEMS,
      current: 3
    };

    //---

    function sendShipment() {
      modalInput.dateTime(gettextCatalog.getString('Date and time shipment was sent'),
                          gettextCatalog.getString('Time sent'),
                          new Date(),
                          { required: true }).result
        .then(function (timeSent) {
          return vm.shipment.send(timeService.dateAndTimeToUtcString(timeSent))
            .catch(notificationsService.updateErrorAndReject);
        })
        .then(function () {
          $state.go('home.shipping');
        });
    }

    function addMoreItems() {
      modalService.modalOkCancel(
        gettextCatalog.getString('Please confirm'),
        gettextCatalog.getString('Are you sure you want to add more items to this shipment?'))
        .then(function () {
          return vm.shipment.created()
            .catch(notificationsService.updateErrorAndReject);
        })
        .then(function () {
          $state.go('home.shipping.addItems', { shipmentId: vm.shipment.id });
        });
    }
  }

  return component;
});
