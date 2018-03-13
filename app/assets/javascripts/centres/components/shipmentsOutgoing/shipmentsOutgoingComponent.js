/**
 * AngularJS Components used in {@link domain.centres.Shipment Shipping}
 *
 * @namespace centres.components.shipmentsOutgoing
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/*
 * Controller for this component
 */
class ShipmentsOutgoingController {

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
    this.shipmentTypes = this.SHIPMENT_TYPES.OUTGOING;
    this.$scope.$emit('tabbed-page-update', 'tab-selected');
  }

}

/**
 * An AngularJS component that allows the user to view the outgoing {@link domain.centres.Shipment Shipments}
 * for a {@link domain.centres.Centre Centre}.
 *
 * @memberOf centres.components.shipmentsOutgoing
 *
 * @param {domain.centres.Centre} centre - the centre to display outgoing shipments for.
 */
const shipmentsOutgoingComponent = {
  template: require('./shipmentsOutgoing.html'),
  controller: ShipmentsOutgoingController,
  controllerAs: 'vm',
  bindings: {
    centre: '<'
  }
};

export default ngModule => ngModule.component('shipmentsOutgoing', shipmentsOutgoingComponent)
