/**
 * AngularJS Component used for displaying shipment specimens.
 *
 * @namespace centres.components.shipments.specimenTableAction
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/**
 * Allows for buttons to be added to a table that displays Shipment Specimens.
 *
 * @memberOf centres.components.shipments.specimenTableAction
 */
const component = {
  template: require('./specimenTableAction.html'),
  controller: SpecimenTableActionDirective,
  controllerAs: 'vm',
  bindings: {
    action:            '<',
    onActionSelected:  '&'
  }
};

/*
 * Controller for this component.
 */
function SpecimenTableActionDirective() {
}

export default ngModule => ngModule.component('specimenTableAction', component)
