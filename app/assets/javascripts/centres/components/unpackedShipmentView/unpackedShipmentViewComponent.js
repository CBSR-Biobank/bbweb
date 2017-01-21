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
    'modalService',
    'notificationsService',
    'shipmentReceiveProgressItems',
    'gettextCatalog'
  ];

  /**
   * FIXME: if the shipment has unpacked items, it cannot be returned to the previous state.
   */
  function UnpackedShipmentViewController($controller,
                                          $scope,
                                          $state,
                                          modalService,
                                          notificationsService,
                                          shipmentReceiveProgressItems,
                                          gettextCatalog) {
    var vm = this;

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
        sref: 'home.shipping.unpack.info',
        active: true
      },
      {
        heading: gettextCatalog.getString('Unpack specimens'),
        sref: 'home.shipping.unpack.unpack',
        active: false
      },
      {
        heading: gettextCatalog.getString('Received specimens'),
        sref: 'home.shipping.unpack.received',
        active: false
      },
      {
        heading: gettextCatalog.getString('Missing specimens'),
        sref: 'home.shipping.unpack.missing',
        active: false
      },
      {
        heading: gettextCatalog.getString('Extra specimens'),
        sref: 'home.shipping.unpack.extra',
        active: false
      }
    ];

    vm.progressInfo = {
      items: shipmentReceiveProgressItems,
      current: 3
    };

    vm.returnToReceivedState = returnToReceivedState;

    //----

    function returnToReceivedState() {
      modalService.modalOkCancel(
        gettextCatalog.getString('Please confirm'),
        gettextCatalog.getString('Are you sure you want to place this shipment in <b>received</b> state?'))
        .then(function () {
          return vm.shipment.receive(vm.shipment.timeReceived)
            .then(function () {
              $state.go('home.shipping.shipment', { shipmentId: vm.shipment.id }, { reload: true });
            })
            .catch(notificationsService.updateError);
        });

    }

  }

  return component;
});
