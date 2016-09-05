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
      shipment: '<'
    }
  };

  ShipmentViewSentController.$inject = [
    '$state',
    'gettextCatalog',
    'modalInput',
    'notificationsService',
    'timeService',
    'stateHelper',
    'modalService',
    'shipmentReceiveProgressItems'
  ];

  /**
   *
   */
  function ShipmentViewSentController($state,
                                      gettextCatalog,
                                      modalInput,
                                      notificationsService,
                                      timeService,
                                      stateHelper,
                                      modalService,
                                      shipmentReceiveProgressItems) {
    var vm = this;

    vm.receiveShipment = receiveShipment;
    vm.returnToPackedState = returnToPackedState;

    vm.progressInfo = {
      items: shipmentReceiveProgressItems,
      current: 1
    };

    //----

    function receiveShipment() {
      if (_.isUndefined(vm.timeReceived)) {
        vm.timeReceived = new Date();
      }
      return modalInput.dateTime(gettextCatalog.getString('Date and time shipment was received'),
                                 gettextCatalog.getString('Time received'),
                                 vm.timeReceived,
                                 { required: true }).result
        .then(function (timeReceived) {
          return vm.shipment.received(timeService.dateToUtcString(timeReceived))
            .then(stateHelper.reloadAndReinit)
            .catch(notificationsService.updateError);
        });
    }

    function returnToPackedState() {
      modalService.modalOkCancel(
        gettextCatalog.getString('Please confirm'),
        gettextCatalog.getString('Are you sure you want to place this shipment in <b>packed</b> state?'))
        .then(function () {
          return vm.shipment.packed(vm.shipment.timePacked)
            .then(stateHelper.reloadAndReinit)
            .catch(notificationsService.updateError);
        });
    }
  }

  return component;
});
