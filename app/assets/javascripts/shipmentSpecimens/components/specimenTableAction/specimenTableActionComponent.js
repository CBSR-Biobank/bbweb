/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
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
