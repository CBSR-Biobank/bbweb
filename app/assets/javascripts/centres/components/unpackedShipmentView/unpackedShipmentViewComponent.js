/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  var component = {
    templateUrl : '/assets/javascripts/centres/components/unpackedShipmentView/unpackedShipmentView.html',
    controller: UnpackedShipmentViewController,
    controllerAs: 'vm',
    bindings: {
      shipment:  '<'
    }
  };

  UnpackedShipmentViewController.$inject = [
    '$controller',
    '$scope',
    '$state',
    'Shipment',
    'ShipmentSpecimen',
    'ShipmentItemState',
    'modalService',
    'modalInput',
    'notificationsService',
    'timeService',
    'SHIPMENT_RECEIVE_PROGRESS_ITEMS',
    'gettextCatalog'
  ];

  /*
   * Controller for this component.
   */
  function UnpackedShipmentViewController($controller,
                                          $scope,
                                          $state,
                                          Shipment,
                                          ShipmentSpecimen,
                                          ShipmentItemState,
                                          modalService,
                                          modalInput,
                                          notificationsService,
                                          timeService,
                                          SHIPMENT_RECEIVE_PROGRESS_ITEMS,
                                          gettextCatalog) {
    var vm = this;

    vm.$onInit = onInit;

    // initialize this controller's base class
    $controller('TabbedPageController',
                {
                  vm:     vm,
                  $scope: $scope,
                  $state: $state
                });

    vm.active = 0;
    vm.tabs = [
      {
        heading: gettextCatalog.getString('Information'),
        sref: 'home.shipping.shipment.unpack.info',
        active: true
      },
      {
        heading: gettextCatalog.getString('Unpack specimens'),
        sref: 'home.shipping.shipment.unpack.unpack',
        active: false
      },
      {
        heading: gettextCatalog.getString('Received specimens'),
        sref: 'home.shipping.shipment.unpack.received',
        active: false
      },
      {
        heading: gettextCatalog.getString('Missing specimens'),
        sref: 'home.shipping.shipment.unpack.missing',
        active: false
      },
      {
        heading: gettextCatalog.getString('Extra specimens'),
        sref: 'home.shipping.shipment.unpack.extra',
        active: false
      }
    ];

    vm.timeCompleted = new Date();

    vm.progressInfo = {
      items: SHIPMENT_RECEIVE_PROGRESS_ITEMS,
      current: 3
    };

    vm.returnToReceivedState = returnToReceivedState;
    vm.completeShipment = completeShipment;

    //----

    function onInit() {
      // get shipment again to get latest version
      return Shipment.get(vm.shipment.id).then(function (shipment) {
        vm.shipment = shipment;
      });
    }

    function cannotGoBackToReceivedModal() {
      modalService.modalOk(
        gettextCatalog.getString('Cannot change state'),
        gettextCatalog.getString('Cannot return this shipment to <b>Received</b> state since ' +
                                 'specimens have already been unpacked.'));
    }

    function backToReceived() {
      modalService.modalOkCancel(
        gettextCatalog.getString('Please confirm'),
        gettextCatalog.getString('Are you sure you want to place this shipment in <b>Received</b> state?'))
        .then(function () {
          return vm.shipment.receive(vm.shipment.timeReceived)
            .catch(notificationsService.updateErrorAndReject);
        })
        .then(function () {
          $state.go('home.shipping.shipment', { shipmentId: vm.shipment.id }, { reload: true });
        });
    }

    function returnToReceivedState() {
      ShipmentSpecimen.list(vm.shipment.id, { filter: 'state:out:' + ShipmentItemState.PRESENT })
        .then(function (pagedResult) {
          var hasNonPresentSpecimens = pagedResult.items.length > 0;

          if (hasNonPresentSpecimens) {
            cannotGoBackToReceivedModal();
            return;
          }

          backToReceived();
        });
    }

    function cannotCompleteShipmentModal() {
      modalService.modalOk(
        gettextCatalog.getString('Cannot change state'),
        gettextCatalog.getString('Cannot place this shipment in <b>Completed</b> state since ' +
                                 'it still has specimens that need unpacking.'));
    }

    function completeShipmentConfirm() {
      modalInput.dateTime(gettextCatalog.getString('Date and time shipment was completed'),
                          gettextCatalog.getString('Time completed'),
                          vm.timeCompleted,
                          { required: true }).result
        .then(function (timeCompleted) {
          return vm.shipment.complete(timeService.dateAndTimeToUtcString(timeCompleted))
            .catch(notificationsService.updateError);
        })
        .then(function () {
          $state.go('home.shipping.shipment', { shipmentId: vm.shipment.id }, { reload: true });
        });
    }

    function completeShipment() {
      ShipmentSpecimen.list(vm.shipment.id, { filter: 'state:in:' + ShipmentItemState.PRESENT })
        .then(function (pagedResult) {
          var hasPresentSpecimens = pagedResult.items.length > 0;

          if (hasPresentSpecimens) {
            cannotCompleteShipmentModal();
            return;
          }

          completeShipmentConfirm();
        });
    }

  }

  return component;
});
