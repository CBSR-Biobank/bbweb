/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var _ = require('lodash');

  var component = {
    templateUrl : '/assets/javascripts/centres/components/shipmentViewSent/shipmentViewSent.html',
    controller: ShipmentViewSentController,
    controllerAs: 'vm',
    bindings: {
      shipmentId: '<'
    }
  };

  ShipmentViewSentController.$inject = [
    '$state',
    'gettext',
    'modalInput',
    'notificationsService',
    'timeService'
  ];

  /**
   *
   */
  function ShipmentViewSentController($state,
                                      gettext,
                                      modalInput,
                                      notificationsService,
                                      timeService) {
    var vm = this;

    vm.receiveShipment = receiveShipment;

    function receiveShipment() {
      if (_.isUndefined(vm.timeReceived)) {
        vm.timeReceived = new Date();
      }
      return modalInput.dateTime(gettext('Date and time shipment was received'),
                                 gettext('Time received'),
                                 vm.timeReceived,
                                 { required: true }).result
        .then(function (timeReceived) {
          return vm.shipment.received(timeService.dateToUtcString(timeReceived));
        })
        .then(function (shipment) {
          return $state.go('home.shipping');
        })
        .catch(notificationsService.updateError);
    }
  }

  return component;
});
