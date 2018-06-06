/**
 * AngularJS Components used in {@link domain.centres.Shipment Shipping}
 *
 * @namespace centres.components.shipmentAddItems
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/*
 * Allows the user to add items to a shipment.
 *
 * A task progress bar is used to give feedback to the user that this is one step in a multi-step process.
 */
/* @ngInject */
function ShipmentAddItemsController($q,
                                    $state,
                                    gettextCatalog,
                                    shipmentSendTasksService,
                                    Shipment,
                                    ShipmentState,
                                    modalInput,
                                    modalService,
                                    timeService,
                                    notificationsService,
                                    domainNotificationService,
                                    shipmentSkipToSentModalService,
                                    breadcrumbService) {
  var vm = this;
  vm.$onInit = onInit;

  //--

  function onInit() {
    vm.breadcrumbs = [
      breadcrumbService.forState('home'),
      breadcrumbService.forState('home.shipping'),
      breadcrumbService.forStateWithFunc('home.shipping.addItems', function () {
        return gettextCatalog.getString(
          '{{courierName}} - {{trackingNumber}}: Items to ship',
          {
            courierName:    vm.shipment.courierName,
            trackingNumber: vm.shipment.trackingNumber
          });

      })
    ];

    vm.timePacked     = new Date();
    vm.tagAsPacked    = tagAsPacked;
    vm.tagAsSent      = tagAsSent;
    vm.removeShipment = removeShipment;

    vm.progressInfo = shipmentSendTasksService.getTaskData().map((taskInfo, index) => {
      taskInfo.status = (index < 2);
      return taskInfo;
    });
  }

  function validateStateChangeAllowed() {
    return Shipment.get(vm.shipment.id).then(function (shipment) {
      if (shipment.specimenCount <= 0) {
        modalService.modalOk(gettextCatalog.getString('Shipment has no specimens'),
                             gettextCatalog.getString('Please add specimens to this shipment fist.'));
        return $q.reject(false);
      }

      return $q.when(true);
    });
  }

  /*
   * Invoked by user when all items have been added to the shipment and it is now packed.
   */
  function tagAsPacked() {
    return validateStateChangeAllowed().then(function () {
      return modalInput.dateTime(gettextCatalog.getString('Date and time shipment was packed'),
                                 gettextCatalog.getString('Time packed'),
                                 vm.timePacked,
                                 { required: true }).result
        .then(timePacked => vm.shipment.pack(timeService.dateAndTimeToUtcString(timePacked)))
        .catch(err => notificationsService.updateError(err))
        .then(shipment => $state.go('home.shipping.shipment', { shipmentId: shipment.id}));
    });
  }

  function tagAsSent() {
    return validateStateChangeAllowed().then(function () {
      vm.timePacked = new Date();
      vm.timeSent = new Date();
      return shipmentSkipToSentModalService.open().result
        .then(function (timeResult) {
          return vm.shipment.skipToStateSent(timeService.dateAndTimeToUtcString(timeResult.timePacked),
                                             timeService.dateAndTimeToUtcString(timeResult.timeSent))
            .then(function () {
              return $state.go('home.shipping.shipment',
                               { shipmentId: vm.shipment.id },
                               { reload: true });
            })
            .catch(function (err) {
              if (err.message === 'TimeSentBeforePacked') {
                err.message = 'the time sent is before the time shipment was packed';
              }
              notificationsService.updateError(err);
            });
        });
    });
  }

  function removeShipment() {
    if (!vm.shipment) { return; }

    domainNotificationService.removeEntity(
      doRemove,
      gettextCatalog.getString('Remove shipment'),
      gettextCatalog.getString('Are you sure you want to remove shipment {{trackingNumber}}?',
                               { trackingNumber: vm.shipment.trackingNumber }),
      gettextCatalog.getString('Remove failed'),
      gettextCatalog.getString('Shipment {{trackingNumber}} cannot be removed',
                               { trackingNumber: vm.shipment.trackingNumber }));

    function doRemove() {
      return vm.shipment.remove().then(function () {
        notificationsService.success(gettextCatalog.getString('Shipment removed'));
        $state.go('home.shipping');
      });
    }
  }
}

/**
 * An AngularJS component that lets the user add {@link domain.participants.Specimen Specimens} or
 * {@link domain.centres.Container Containers} to a {@link domain.centres.Shipment Shipment}.
 *
 * After the user adds an item, this component also lets the user tag the shipment as {@link
 * domain.centres.ShipmentState PACKED} or {@link domain.centres.ShipmentState SENT}.
 *
 * @memberOf centres.components.shipmentAddItems
 *
 * @param {domain.centres.Shipment} shipment - the shipment the items will be added to.
 */
const shipmentAddItemsComponent = {
  template: require('./shipmentAddItems.html'),
  controller: ShipmentAddItemsController,
  controllerAs: 'vm',
  bindings: {
    shipment: '<'
  }
};

export default ngModule => ngModule.component('shipmentAddItems', shipmentAddItemsComponent)
