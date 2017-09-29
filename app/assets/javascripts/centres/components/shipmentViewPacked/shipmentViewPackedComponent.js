/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  var component = {
    template: require('./shipmentViewPacked.html'),
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

  function ShipmentViewPackedController($q,
                                        $state,
                                        gettextCatalog,
                                        SHIPMENT_SEND_PROGRESS_ITEMS,
                                        modalInput,
                                        notificationsService,
                                        timeService,
                                        modalService) {
    var vm = this;
    vm.$onInit = onInit;

    //--

    function onInit() {
      vm.timeSent     = new Date();
      vm.sendShipment = sendShipment;
      vm.addMoreItems = addMoreItems;

      vm.progressInfo = {
        items: SHIPMENT_SEND_PROGRESS_ITEMS,
        current: 3
      };
    }

    function sendShipment() {
      modalInput.dateTime(gettextCatalog.getString('Date and time shipment was sent'),
                          gettextCatalog.getString('Time sent'),
                          vm.timeSent,
                          { required: true }).result
        .then(function (timeSent) {
          return vm.shipment.send(timeService.dateAndTimeToUtcString(timeSent))
            .then(function () {
              $state.go('home.shipping');
            })
            .catch(function (err) {
              if (err.message === 'TimeSentBeforePacked') {
                err.message = gettextCatalog.getString('The sent time is before the packed time');
              }
              notificationsService.updateError(err);
            });
        });
    }

    function addMoreItems() {
      modalService.modalOkCancel(
        gettextCatalog.getString('Please confirm'),
        gettextCatalog.getString('Are you sure you want to add more items to this shipment?'))
        .then(function () {
          return vm.shipment.created()
            .then(function () {
              $state.go('home.shipping.addItems', { shipmentId: vm.shipment.id });
            })
            .catch(notificationsService.updateError);
        });
    }
  }

  return component;
});
