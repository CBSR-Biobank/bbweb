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
    'gettext',
    'shipmentProgressItems',
    'modalInput',
    'notificationsService',
    'timeService'
  ];

  /**
   *
   */
  function ShipmentViewPackedController($state,
                                        gettext,
                                        shipmentProgressItems,
                                        modalInput,
                                        notificationsService,
                                        timeService) {
    var vm = this;

    vm.sendShipment = sendShipment;
    vm.progressInfo = {
      items: shipmentProgressItems,
      current: 3
    };

    function sendShipment() {
      if (_.isUndefined(vm.timeSent)) {
        vm.timeSent = new Date();
      }
      return modalInput.dateTime(gettext('Date and time shipment was sent'),
                                 gettext('Time sent'),
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
  }

  return component;
});
