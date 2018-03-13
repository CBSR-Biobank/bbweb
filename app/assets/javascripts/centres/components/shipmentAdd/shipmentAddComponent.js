/**
 * AngularJS Components used in {@link domain.centres.Shipment Shipping}
 *
 * @namespace centres.components.shipmentAdd
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash'

/*
 * Controller for this component.
 */
/* @ngInject */
function ShipmentAddController($state,
                               gettextCatalog,
                               Centre,
                               Shipment,
                               domainNotificationService,
                               notificationsService,
                               shipmentSendTasksService,
                               breadcrumbService) {
  var vm = this;
  vm.$onInit = onInit;

  //--

  function onInit() {
    vm.breadcrumbs = [
      breadcrumbService.forState('home'),
      breadcrumbService.forState('home.shipping'),
      breadcrumbService.forState('home.shipping.add')
    ];

    vm.progressInfo = shipmentSendTasksService.getTaskData().map((taskInfo, index) => {
      taskInfo.status = (index < 1);
      return taskInfo;
    });

    vm.hasValidCentres           = false;
    vm.shipment                  = new Shipment();
    vm.submit                    = submit;
    vm.cancel                    = cancel;
    vm.getFromCentreLocationInfo = getFromCentreLocationInfo;
    vm.getToCentreLocationInfo   = getToCentreLocationInfo;

    return Centre.locationsSearch().then(function (results) {
      vm.hasValidCentres = (results.length > 1);
    });
  }

  function submit() {
    vm.shipment.add().then(onAddSuccessful).catch(onAddFailed);

    function onAddSuccessful(shipment) {
      $state.go('home.shipping.addItems', { shipmentId: shipment.id });
    }

    function onAddFailed(error) {
      domainNotificationService.updateErrorModal(error, gettextCatalog.getString('shipment'));
    }
  }

  function cancel() {
    $state.go('home.shipping');
  }

  function getCentreLocationInfo(filter, locationIdsToOmit) {
    return Centre.locationsSearch(filter)
      .then(function (locations) {
        _.remove(locations, function (location) {
          return _.includes(locationIdsToOmit, location.locationId);
        });
        return locations;
      });
  }

  function getFromCentreLocationInfo(filter) {
    var locationIdsToOmit = [];
    if (vm.shipment.toLocationInfo) {
      locationIdsToOmit.push(vm.shipment.toLocationInfo.locationId);
    }
    return getCentreLocationInfo(filter, locationIdsToOmit);
  }

  function getToCentreLocationInfo(filter) {
    var locationIdsToOmit = [];
    if (vm.shipment.fromLocationInfo) {
      locationIdsToOmit.push(vm.shipment.fromLocationInfo.locationId);
    }
    return getCentreLocationInfo(filter, locationIdsToOmit);
  }
}

/**
 * An AngularJS component that lets the user add a {@link domain.centres.Shipment Shipment}.
 *
 * @memberOf centres.components.shipmentAdd
*/
const shipmentAddComponent = {
  template: require('./shipmentAdd.html'),
  controller: ShipmentAddController,
  controllerAs: 'vm',
  bindings: {
  }
};

export default ngModule => ngModule.component('shipmentAdd', shipmentAddComponent)
