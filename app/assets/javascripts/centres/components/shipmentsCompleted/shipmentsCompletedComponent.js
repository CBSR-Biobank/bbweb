/**
 * AngularJS Components used in {@link domain.centres.Shipment Shipping}
 *
 * @namespace centres.components.shipmentsCompleted
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/*
 * Controller for this component.
 */
class ShipmentsCompletedController {

  constructor($scope,
              Shipment,
              ShipmentState,
              SHIPMENT_TYPES) {
    'ngInject'
    Object.assign(this, {
      $scope,
      Shipment,
      ShipmentState,
      SHIPMENT_TYPES
    })
  }

  $onInit() {
    this.shipmentTypes = this.SHIPMENT_TYPES.COMPLETED;
    this.$scope.$emit('tabbed-page-update', 'tab-selected');
  }

}

/**
 * An AngularJS component that displays the completed {@link domain.centres.Shipment Shipments} a {@link
 * domain.centres.Centre Centre} has **sent** or **received**.
 *
 * @memberOf centres.components.shipmentsCompleted
 *
 * @param {domain.centres.Centre} centre - the centre to display completed shipments for.
 */
const shipmentsCompletedComponent = {
  template: require('./shipmentsCompleted.html'),
  controller: ShipmentsCompletedController,
  controllerAs: 'vm',
  bindings: {
    centre: '<'
  }
};

export default ngModule => ngModule.component('shipmentsCompleted', shipmentsCompletedComponent)
