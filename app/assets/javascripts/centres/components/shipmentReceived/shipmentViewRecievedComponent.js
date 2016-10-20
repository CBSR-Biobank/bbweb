/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var _ = require('lodash');

  var component = {
    templateUrl : '/assets/javascripts/centres/components/shipmentViewReceived/shipmentViewReceived.html',
    controller: ShipmentViewReceivedController,
    controllerAs: 'vm',
    bindings: {
      shipmentId: '<'
    }
  };

  ShipmentViewReceivedController.$inject = [
    '$state',
    'gettextCatalog',
    'modalInput',
    'notificationsService',
    'timeService'
  ];

  /**
   *
   */
  function ShipmentViewReceivedController($state,
                                          gettextCatalog,
                                          modalInput,
                                          notificationsService,
                                          timeService) {
    var vm = this;

    vm.receiveShipment = receiveShipment;

    function receiveShipment() {
      if (_.isUndefined(vm.timeUnpacked)) {
        vm.timeUnpacked = new Date();
      }
      return modalInput.dateTime(gettextCatalog.getString('Date and time shipment was unpacked'),
                                 gettextCatalog.getString('Time received'),
                                 vm.timeUnpacked,
                                 { required: true })
        .result
        .then(function (timeUnpacked) {
          return vm.shipment.received(timeService.dateAndTimeToUtcString(timeUnpacked));
        })
        .then(function (shipment) {
          return $state.go('home.shipping');
        })
        .catch(notificationsService.updateError);
    }
  }

  return component;
});
