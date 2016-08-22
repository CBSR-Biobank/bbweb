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
    'Centre',
    'shipmentProgressItems',
    'modalInput',
    'notificationsService'
  ];

  /**
   *
   */
  function ShippingInfoViewController($filter,
                                      Centre,
                                      shipmentProgressItems,
                                      modalInput,
                                      notificationsService) {
    var vm = this;

    vm.notificationTimeout = 1500;
    vm.panelOpen = true;

    vm.$onInit            = onInit;
    vm.panelButtonClicked = panelButtonClicked;


    //--

    function onInit() {
      commonDisplayProperties();
      displayPropertiesByState();
    }

    function commonDisplayProperties() {
      var properties = {
        courier:        new DisplayProperty('Courier',         vm.shipment.courierName),
        trackingNumber: new DisplayProperty('Tracking Number', vm.shipment.trackingNumber),
        fromLocation:   new DisplayProperty('From centre',     vm.shipment.fromLocationInfo.name),
        toLocation:     new DisplayProperty('To centre',       vm.shipment.toLocationInfo.name)
      };

      if (!vm.readOnly) {
        properties.courier.allowEdit(editCourierName, 'Update courier');
        properties.trackingNumber.allowEdit(editTrackingNumber, 'Update tracking number');
        properties.fromLocation.allowEdit(editFromLocation, 'Update from location');
        properties.toLocation.allowEdit(editToLocation, 'Update to location');
      }

      vm.displayProperties = _.values(properties);
    }

    function displayPropertiesByState() {
      if (vm.shipment.timePacked) {
        vm.displayProperties.push(new DisplayProperty('Time packed',
                                                      $filter('localTime')(vm.shipment.timePacked)));
      }

      if (vm.shipment.timeSent) {
        vm.displayProperties.push(new DisplayProperty('Time sent',
                                                      $filter('localTime')(vm.shipment.timeSent)));
      }

      if (vm.shipment.timeReceived) {
        vm.displayProperties.push(new DisplayProperty('Time received',
                                                      $filter('localTime')(vm.shipment.timeReceived)));
      }

      if (vm.shipment.isNotCreatedOrUnpacked()) {
        vm.displayProperties.push(new DisplayProperty('Number of specimens', vm.shipment.specimenCount));

        if (vm.shipment.containerCount) {
          vm.displayProperties.push(new DisplayProperty('Number of containers', vm.shipment.specimenCount));
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
      modalInput.text('Edit courier', 'Courier', vm.shipment.courierName, { required: true, minLength: 2 })
        .result.then(function (name) {
          vm.shipment.updateCourierName(name)
            .then(postUpdate('Courier changed successfully.', 'Change successful'))
            .catch(notificationsService.updateError);
        });
    }

    function editTrackingNumber() {
      modalInput.text('Edit tracking number',
                      'Tracking Number',
                      vm.shipment.trackingNumber,
                      { required: true, minLength: 2 })
        .result.then(function (tn) {
          vm.shipment.updateTrackingNumber(tn)
            .then(postUpdate('Tracking number changed successfully.', 'Change successful'))
            .catch(notificationsService.updateError);
        });
    }

    function editFromLocation() {
      editLocation('Update from centre',
                   'From centre',
                   vm.shipment.fromLocationInfo.name,
                   vm.shipment.toLocationInfo.name,
                   'From location changed successfully.');
    }

    function editToLocation() {
      editLocation('Update to centre',
                   'To centre',
                   vm.shipment.toLocationInfo.name,
                   vm.shipment.fromLocationInfo.name,
                   'To location changed successfully.');
    }

    function editLocation(title,
                          label,
                          defaultValue,
                          locationNameToOmit,
                          notificationMessage) {
      return Centre.allLocations()
        .then(Centre.centreLocationToNames)
        .then(function (centreLocations) {
          return _.keyBy(centreLocations, 'name');
        })
        .then(function (centreLocationsByName) {
          var validLocationNames = _(centreLocationsByName).omit([ locationNameToOmit ]).keys().value();
          modalInput.select(title,
                            label,
                            defaultValue,
                            { required: true, selectOptions: validLocationNames }).result
            .then(function (selection) {
              if (selection) {
                var centreLocation = centreLocationsByName[selection];

                if (!centreLocation) {
                  throw new Error('centre location lookup by name failed: ' + selection);
                }

                vm.shipment.updateFromLocation(centreLocation.locationId)
                  .then(postUpdate(notificationMessage, 'Change successful'))
                  .catch(notificationsService.updateError);
              }
            });
        });
    }

  }

  return component;
});
