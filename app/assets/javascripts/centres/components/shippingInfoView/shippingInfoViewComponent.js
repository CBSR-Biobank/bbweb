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
    'gettext',
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
                                      gettext,
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
        courier:        new DisplayProperty(gettext('Courier'),         vm.shipment.courierName),
        trackingNumber: new DisplayProperty(gettext('Tracking Number'), vm.shipment.trackingNumber),
        fromLocation:   new DisplayProperty(gettext('From centre'),     vm.shipment.fromLocationInfo.name),
        toLocation:     new DisplayProperty(gettext('To centre'),       vm.shipment.toLocationInfo.name)
      };

      if (!vm.readOnly) {
        properties.courier.allowEdit(editCourierName,           gettext('Update courier'));
        properties.trackingNumber.allowEdit(editTrackingNumber, gettext('Update tracking number'));
        properties.fromLocation.allowEdit(editFromLocation,     gettext('Update from location'));
        properties.toLocation.allowEdit(editToLocation,         gettext('Update to location'));
      }

      vm.displayProperties = _.values(properties);
    }

    function displayPropertiesByState() {
      if (vm.shipment.timePacked) {
        vm.displayProperties.push(new DisplayProperty(gettext('Time packed'),
                                                      $filter('localTime')(vm.shipment.timePacked)));
      }

      if (vm.shipment.timeSent) {
        vm.displayProperties.push(new DisplayProperty(gettext('Time sent'),
                                                      $filter('localTime')(vm.shipment.timeSent)));
      }

      if (vm.shipment.timeReceived) {
        vm.displayProperties.push(new DisplayProperty(gettext('Time received'),
                                                      $filter('localTime')(vm.shipment.timeReceived)));
      }

      if (vm.shipment.isNotCreatedOrUnpacked()) {
        vm.displayProperties.push(new DisplayProperty(gettext('Number of specimens'),
                                                      vm.shipment.specimenCount));

        if (vm.shipment.containerCount) {
          vm.displayProperties.push(new DisplayProperty(gettext('Number of containers'),
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
      modalInput.text(gettext('Edit courier'),
                      gettext('Courier'),
                      vm.shipment.courierName,
                      { required: true, minLength: 2 }).result
        .then(function (name) {
          vm.shipment.updateCourierName(name)
            .then(postUpdate(gettext('Courier changed successfully.'), gettext('Change successful')))
            .catch(notificationsService.updateError);
        });
    }

    function editTrackingNumber() {
      modalInput.text(gettext('Edit tracking number'),
                      gettext('Tracking Number'),
                      vm.shipment.trackingNumber,
                      { required: true, minLength: 2 }).result
        .then(function (tn) {
          vm.shipment.updateTrackingNumber(tn)
            .then(postUpdate(gettext('Tracking number changed successfully.',
                                     gettext('Change successful'))))
            .catch(notificationsService.updateError);
        });
    }

    function editFromLocation() {
      centreLocationsModalService.open(
        gettext('Update from centre'),
        gettext('From centre'),
        gettext('The location of the centre this shipment is coming from'),
        vm.shipment.fromLocationInfo,
        [ vm.shipment.toLocationInfo ]
      ).result.then(function (selection) {
        if (selection) {
          vm.shipment.updateFromLocation(selection.locationId)
            .then(postUpdate(gettext('From location changed successfully.'),
                             gettext('Change successful')))
            .catch(notificationsService.updateError);
        }
      });
    }

    function editToLocation() {
      centreLocationsModalService.open(gettext('Update to centre'),
                                       gettext('To centre'),
                                       gettext('The location of the centre this shipment is going to'),
                                       vm.shipment.toLocationInfo,
                                       [ vm.shipment.fromLocationInfo ]).result
        .then(function (selection) {
          if (selection) {
            vm.shipment.updateToLocation(selection.locationId)
              .then(postUpdate(gettext('To location changed successfully.'),
                               gettext('Change successful')))
              .catch(notificationsService.updateError);
          }
        });
    }

  }

  return component;
});
