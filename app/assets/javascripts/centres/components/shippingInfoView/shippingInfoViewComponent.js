/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var _ = require('lodash');

  var component = {
    templateUrl : '/assets/javascripts/centres/components/shippingInfoView/shippingInfoView.html',
    controller: ShippingInfoViewController,
    controllerAs: 'vm',
    bindings: {
      shipment: '<',
      readOnly: '<',
      collapsable: '<'
    }
  };

  ShippingInfoViewController.$inject = [
    '$filter',
    'gettextCatalog',
    'Centre',
    'shipmentProgressItems',
    'modalInput',
    'notificationsService',
    'centreLocationsModalService'
  ];

  /**
   *
   */
  function ShippingInfoViewController($filter,
                                      gettextCatalog,
                                      Centre,
                                      shipmentProgressItems,
                                      modalInput,
                                      notificationsService,
                                      centreLocationsModalService) {
    var vm = this;

    vm.notificationTimeout = 1500;
    vm.panelOpen = true;

    vm.$onChanges         = onChanges;
    vm.panelButtonClicked = panelButtonClicked;


    //--

    function onChanges(changesObj) {
      if (changesObj.shipment && vm.shipment) {
        commonDisplayProperties();
        displayPropertiesByState();
      }
    }

    function commonDisplayProperties() {
      var properties = {
        courier:        new DisplayProperty(gettextCatalog.getString('Courier'),         vm.shipment.courierName),
        trackingNumber: new DisplayProperty(gettextCatalog.getString('Tracking Number'), vm.shipment.trackingNumber),
        fromLocation:   new DisplayProperty(gettextCatalog.getString('From centre'),     vm.shipment.fromLocationInfo.name),
        toLocation:     new DisplayProperty(gettextCatalog.getString('To centre'),       vm.shipment.toLocationInfo.name)
      };

      if (!vm.readOnly) {
        properties.courier.allowEdit(editCourierName,           gettextCatalog.getString('Update courier'));
        properties.trackingNumber.allowEdit(editTrackingNumber, gettextCatalog.getString('Update tracking number'));
        properties.fromLocation.allowEdit(editFromLocation,     gettextCatalog.getString('Update from location'));
        properties.toLocation.allowEdit(editToLocation,         gettextCatalog.getString('Update to location'));
      }

      vm.displayProperties = _.values(properties);
    }

    function displayPropertiesByState() {
      if (vm.shipment.timePacked) {
        vm.displayProperties.push(new DisplayProperty(gettextCatalog.getString('Time packed'),
                                                      $filter('localTime')(vm.shipment.timePacked)));
      }

      if (vm.shipment.timeSent) {
        vm.displayProperties.push(new DisplayProperty(gettextCatalog.getString('Time sent'),
                                                      $filter('localTime')(vm.shipment.timeSent)));
      }

      if (vm.shipment.timeReceived) {
        vm.displayProperties.push(new DisplayProperty(gettextCatalog.getString('Time received'),
                                                      $filter('localTime')(vm.shipment.timeReceived)));
      }

      if (vm.shipment.isNotCreatedOrUnpacked()) {
        vm.displayProperties.push(new DisplayProperty(gettextCatalog.getString('Number of specimens'),
                                                      vm.shipment.specimenCount));

        if (vm.shipment.containerCount) {
          vm.displayProperties.push(new DisplayProperty(gettextCatalog.getString('Number of containers'),
                                                        vm.shipment.specimenCount));
        }
      }
    }

    function DisplayProperty(label, value, editFunc, buttonTitle) {
      this.label = label;
      this.value = value;
      if (editFunc) {
        this.editFunc = editFunc;
      }
      if (buttonTitle) {
        this.buttonTitle = buttonTitle;
      }
    }

    DisplayProperty.prototype.allowEdit = function (editFunc, buttonTitle) {
      this.editFunc = editFunc;
      if (buttonTitle) {
        this.buttonTitle = buttonTitle;
      }
    };

    function panelButtonClicked() {
      vm.panelOpen = !vm.panelOpen;
    }

    function postUpdate(message, title, timeout) {
      timeout = timeout || vm.notificationTimeout;
      return function (shipment) {
        vm.shipment = shipment;
        notificationsService.success(message, title, timeout);
      };
    }

    function editCourierName() {
      modalInput.text(gettextCatalog.getString('Edit courier'),
                      gettextCatalog.getString('Courier'),
                      vm.shipment.courierName,
                      { required: true, minLength: 2 }).result
        .then(function (name) {
          vm.shipment.updateCourierName(name)
            .then(postUpdate(gettextCatalog.getString('Courier changed successfully.'), gettextCatalog.getString('Change successful')))
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
            .then(postUpdate(gettextCatalog.getString('Tracking number changed successfully.',
                                     gettextCatalog.getString('Change successful'))))
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
      centreLocationsModalService.open(gettextCatalog.getString('Update to centre'),
                                       gettextCatalog.getString('To centre'),
                                       gettextCatalog.getString('The location of the centre this shipment is going to'),
                                       vm.shipment.toLocationInfo,
                                       [ vm.shipment.fromLocationInfo ]).result
        .then(function (selection) {
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
