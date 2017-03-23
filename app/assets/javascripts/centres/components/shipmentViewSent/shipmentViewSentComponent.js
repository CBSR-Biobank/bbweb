/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var component = {
    templateUrl : '/assets/javascripts/centres/components/shipmentViewSent/shipmentViewSent.html',
    controller: ShipmentViewSentController,
    controllerAs: 'vm',
    bindings: {
      shipment: '<'
    }
  };

  ShipmentViewSentController.$inject = [
    '$q',
    '$state',
    'gettextCatalog',
    'modalInput',
    'notificationsService',
    'timeService',
    'stateHelper',
    'modalService',
    'SHIPMENT_RECEIVE_PROGRESS_ITEMS',
    'shipmentSkipToUnpackedModalService'
  ];

  /*
   * Controller for this component.
   */
  function ShipmentViewSentController($q,
                                      $state,
                                      gettextCatalog,
                                      modalInput,
                                      notificationsService,
                                      timeService,
                                      stateHelper,
                                      modalService,
                                      SHIPMENT_RECEIVE_PROGRESS_ITEMS,
                                      shipmentSkipToUnpackedModalService) {
    var vm = this;

    vm.timeReceived        = new Date();
    vm.returnToPackedState = returnToPackedState;
    vm.tagAsLost           = tagAsLost;
    vm.receiveShipment     = receiveShipment;
    vm.unpackShipment      = unpackShipment;

    vm.progressInfo = {
      items: SHIPMENT_RECEIVE_PROGRESS_ITEMS,
      current: 1
    };

    //----

    function returnToPackedState() {
      modalService.modalOkCancel(
        gettextCatalog.getString('Please confirm'),
        gettextCatalog.getString('Are you sure you want to place this shipment in <b>Packed</b> state?'))
        .then(function () {
          return vm.shipment.pack(vm.shipment.timePacked)
            .catch(notificationsService.updateErrorAndReject);
        })
        .then(stateHelper.reloadAndReinit);
    }

    function tagAsLost() {
      modalService.modalOkCancel(
        gettextCatalog.getString('Please confirm'),
        gettextCatalog.getString('Are you sure you want to tag this shipment as <b>Lost</b>?'))
        .then(function () {
          return vm.shipment.lost()
            .catch(notificationsService.updateErrorAndReject);
        })
        .then(stateHelper.reloadAndReinit);
    }

    function receiveShipment() {
      modalInput.dateTime(gettextCatalog.getString('Date and time shipment was received'),
                          gettextCatalog.getString('Time received'),
                          vm.timeReceived,
                          { required: true }).result
        .then(function (timeReceived) {
          return vm.shipment.receive(timeService.dateAndTimeToUtcString(timeReceived))
            .catch(function (err) {
              if (err.message === 'TimeReceivedBeforeSent') {
                err.message = gettextCatalog.getString('The received time is before the sent time');
              }
              notificationsService.updateError(err);
              return $q.reject(err);
            });
        })
        .then(stateHelper.reloadAndReinit);
    }

    function unpackShipment() {
      return shipmentSkipToUnpackedModalService.open().result
        .then(function (timeResult) {
          return vm.shipment.skipToStateUnpacked(timeService.dateAndTimeToUtcString(timeResult.timeReceived),
                                                 timeService.dateAndTimeToUtcString(timeResult.timeUnpacked))
            .catch(function (err) {
              var newErr = {};
              if (err.message === 'TimeReceivedBeforeSent') {
                newErr.message =
                  gettextCatalog.getString('The received time is before the sent time');
              } else if (err.message === 'TimeUnpackedBeforeReceived') {
                newErr.message =
                  gettextCatalog.getString('The unpacked time is before the received time');
              }
              notificationsService.updateError(newErr);
              return $q.reject(err);
            });
        })
        .then(function (shipment) {
          return $state.go('home.shipping.shipment.unpack.info', { shipmentId: shipment.id});
        });
    }
  }

  return component;
});
