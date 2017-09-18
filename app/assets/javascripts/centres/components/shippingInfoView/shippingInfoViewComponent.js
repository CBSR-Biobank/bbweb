/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  var component = {
    templateUrl : '/assets/javascripts/centres/components/shippingInfoView/shippingInfoView.html',
    controller: ShippingInfoViewController,
    controllerAs: 'vm',
    bindings: {
      shipment:    '<',
      collapsible: '<',
      readOnly:    '<'
    }
  };

  ShippingInfoViewController.$inject = [
    'gettextCatalog',
    'Centre',
    'modalInput',
    'notificationsService',
    'centreLocationsModalService',
    'shipmentStateLabelService'
  ];

  function ShippingInfoViewController(gettextCatalog,
                                      Centre,
                                      modalInput,
                                      notificationsService,
                                      centreLocationsModalService,
                                      shipmentStateLabelService) {
    var vm = this;
    vm.$onInit = onInit;

    //--

    function onInit() {
      vm.editCourierName    = editCourierName;
      vm.editTrackingNumber = editTrackingNumber;
      vm.editFromLocation   = editFromLocation;
      vm.editToLocation     = editToLocation;

      vm.stateLabelFunc = shipmentStateLabelService.stateToLabel(vm.shipment.state);
    }

    function postUpdate(property, message, title) {
      return function (shipment) {
        vm.shipment = shipment;
        notificationsService.success(message, title);
      };
    }

    function editCourierName() {
      modalInput.text(gettextCatalog.getString('Edit courier'),
                      gettextCatalog.getString('Courier'),
                      vm.shipment.courierName,
                      { required: true, minLength: 2 }).result
        .then(function (name) {
          vm.shipment.updateCourierName(name)
            .then(postUpdate(gettextCatalog.getString('Courier changed successfully.'),
                             gettextCatalog.getString('Change successful')))
            .catch(notificationsService.updateError);
        });
    }

    function editTrackingNumber() {
      modalInput.text(gettextCatalog.getString('Edit tracking number'),
                      gettextCatalog.getString('Tracking Number'),
                      vm.shipment.trackingNumber,
                      { required: true, minLength: 2 }).result
        .then(function (tn) {
          vm.shipment.updateTrackingNumber(tn)
            .then(postUpdate(gettextCatalog.getString('Tracking number changed successfully.'),
                             gettextCatalog.getString('Change successful')))
            .catch(notificationsService.updateError);
        });
    }

    function editFromLocation() {
      centreLocationsModalService.open(
        gettextCatalog.getString('Update from centre'),
        gettextCatalog.getString('From centre'),
        gettextCatalog.getString('The location of the centre this shipment is coming from'),
        vm.shipment.fromLocationInfo,
        [ vm.shipment.toLocationInfo ]
      ).result.then(function (selection) {
        if (selection) {
          vm.shipment.updateFromLocation(selection.locationId)
            .then(postUpdate(gettextCatalog.getString('From location changed successfully.'),
                             gettextCatalog.getString('Change successful')))
            .catch(notificationsService.updateError);
        }
      });
    }

    function editToLocation() {
      centreLocationsModalService.open(
        gettextCatalog.getString('Update to centre'),
        gettextCatalog.getString('To centre'),
        gettextCatalog.getString('The location of the centre this shipment is going to'),
        vm.shipment.toLocationInfo,
        [ vm.shipment.fromLocationInfo ]
      ).result.then(function (selection) {
          if (selection) {
            vm.shipment.updateToLocation(selection.locationId)
              .then(postUpdate(gettextCatalog.getString('To location changed successfully.'),
                               gettextCatalog.getString('Change successful')))
              .catch(notificationsService.updateError);
          }
        });
    }

  }

  return component;
});
