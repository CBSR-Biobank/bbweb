/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var _ = require('lodash');

  var component = {
    templateUrl : '/assets/javascripts/centres/components/shipmentViewPacked/shipmentViewPacked.html',
    controller: ShipmentViewPackedController,
    controllerAs: 'vm',
    bindings: {
      shipment: '<'
    }
  };

  ShipmentViewPackedController.$inject = [
    '$state',
    'gettextCatalog',
    'shipmentSendProgressItems',
    'modalInput',
    'notificationsService',
    'timeService',
    'modalService'
  ];

  /**
   *
   */
  function ShipmentViewPackedController($state,
                                        gettextCatalog,
                                        shipmentSendProgressItems,
                                        modalInput,
                                        notificationsService,
                                        timeService,
                                        modalService) {
    var vm = this;

    vm.sendShipment = sendShipment;
    vm.addMoreItems = addMoreItems;

    vm.progressInfo = {
      items: shipmentSendProgressItems,
      current: 3
    };

    //---

    function sendShipment() {
      if (_.isUndefined(vm.timeSent)) {
        vm.timeSent = new Date();
      }
      return modalInput.dateTime(gettextCatalog.getString('Date and time shipment was sent'),
                                 gettextCatalog.getString('Time sent'),
                                 vm.timeSent,
                                 { required: true }).result
        .then(function (timeSent) {
          return vm.shipment.sent(timeService.dateToUtcString(timeSent))
            .then(function (shipment) {
              return $state.go('home.shipping');
            })
            .catch(notificationsService.updateError);
        });
    }

    function addMoreItems() {
      modalService.modalOkCancel(
        gettextCatalog.getString('Please confirm'),
        gettextCatalog.getString('Are you sure you want to add more items to this shipment?'))
        .then(function () {
          return vm.shipment.created(vm.shipment.timePacked)
            .then(function () {
              $state.go('home.shipping.addItems', { shipmentId: vm.shipment.id });
            })
            .catch(notificationsService.updateError);
        });
    }
  }

  return component;
});
