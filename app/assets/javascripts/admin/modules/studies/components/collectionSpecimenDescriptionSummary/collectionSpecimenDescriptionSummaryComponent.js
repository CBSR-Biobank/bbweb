/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */

var component = {
  template: require('./collectionSpecimenDescriptionSummary.html'),
  controller: CollectionSpecimenDescriptionSummaryController,
  controllerAs: 'vm',
  bindings: {
    specimenDescription: '<'
  }
};

/*
 * Controller for this component.
 */
function CollectionSpecimenDescriptionSummaryController() {

}

export default ngModule => ngModule.component('collectionSpecimenDescriptionSummary', component)
