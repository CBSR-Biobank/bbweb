/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */

/*
 * Controller for this component.
 */
class Controller {

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
 *
 */
var COMPONENT = {
  template: require('./shipmentsCompleted.html'),
  controller: Controller,
  controllerAs: 'vm',
  bindings: {
    centre: '<'
  }
};

export default ngModule => ngModule.component('shipmentsCompleted', COMPONENT)
