/**
 * AngularJS Components used in {@link domain.centres.Shipment Shipping}
 *
 * @namespace centres.components.shipmentsIncoming
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/*
 * Controller for this component.
 */
class ShipmentsIncomingController {

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
    this.shipmentTypes = this.SHIPMENT_TYPES.INCOMING;
    this.$scope.$emit('tabbed-page-update', 'tab-selected');
  }

}

/**
 * An AngularJS component that displays the incoming {@link domain.centres.Shipment Shipments} to a {@link
 * domain.centres.Centre Centre}.
 *
 * @memberOf centres.components.shipmentsIncoming
 *
 * @param {domain.centres.Centre} centre - the centre to display incoming shipments for.
 */
const shipmentsIncomingComponent = {
  template: require('./shipmentsIncoming.html'),
  controller: ShipmentsIncomingController,
  controllerAs: 'vm',
  bindings: {
    centre: '<'
  }
};

export default ngModule => ngModule.component('shipmentsIncoming', shipmentsIncomingComponent)
