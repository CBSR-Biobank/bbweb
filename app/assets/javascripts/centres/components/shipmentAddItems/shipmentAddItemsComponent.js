/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(['lodash'], function (_) {
  'use strict';

  var component = {
    templateUrl : '/assets/javascripts/centres/components/shipmentAddItems/shipmentAddItems.html',
    controller: ShipmentAddItemsController,
    controllerAs: 'vm',
    bindings: {
      shipment: '<'
    }
  };

  ShipmentAddItemsController.$inject = [
    'Centre',
    'shipmentProgressItems',
    'modalInput',
    'notificationsService'
  ];

  /**
   *
   */
  function ShipmentAddItemsController(Centre,
                                      shipmentProgressItems,
                                      modalInput,
                                      notificationsService) {
    var vm = this;

    vm.progressInfo = {
      items: shipmentProgressItems,
      current: 2
    };

    vm.centreLocations = [];
    vm.notificationTimeout = 1500;

    vm.editCourierName    = editCourierName;
    vm.editTrackingNumber = editTrackingNumber;
    vm.editFromLocation   = editFromLocation;
    vm.editToLocation     = editToLocation;

    init();

    //--

    function init() {
      return Centre.allLocations().then(function (centreLocations) {
        vm.centreLocations = Centre.centreLocationToNames(centreLocations);
        vm.centreLocationNames = _.map(vm.centreLocations, function (cl) {
          return cl.name;
        });
      });
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
      var locationNames = _.filter(vm.centreLocationNames, function (name) {
        return name !== vm.shipment.toLocationInfo.name;
      });
      modalInput.select('Update from centre',
                        'From centre',
                        vm.shipment.fromLocationInfo.name,
                        {
                          required: true,
                          selectOptions: locationNames
                        })
        .result.then(function (selection) {
          var centreLocation = _.find(vm.centreLocations, function (cl) {
            return cl.name === selection;
          });
          if (centreLocation) {
            vm.shipment.updateFromLocation(centreLocation.locationId)
            .then(postUpdate('From location changed successfully.', 'Change successful'))
            .catch(notificationsService.updateError);
          }
        });
    }

    function editToLocation() {
      var locationNames = _.filter(vm.centreLocationNames, function (name) {
        return name !== vm.shipment.fromLocationInfo.name;
      });
      modalInput.select('Update to centre',
                        'To centre',
                        vm.shipment.toLocationInfo.name,
                        {
                          required: true,
                          selectOptions: locationNames
                        })
        .result.then(function (selection) {
          var centreLocation = _.find(vm.centreLocations, function (cl) {
            return cl.name === selection;
          });
          if (centreLocation) {
            vm.shipment.updateToLocation(centreLocation.locationId)
            .then(postUpdate('To location changed successfully.', 'Change successful'))
            .catch(notificationsService.updateError);
          }
        });
    }

  }

  return component;
});
