/**
 * AngularJS Component for {@link domain.studies.CollectionEventType CollectionEventType} administration.
 *
 * @namespace admin.studies.components.collectionSpecimenDescriptionSummary
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/*
 * Controller for this component.
 */
function CollectionSpecimenDescriptionSummaryController() {

}

/**
 * An AngularJS component that displays summary information for a {@link
 * domain.studies.CollectionSpecimenDescription CollectionSpecimenDescription}
 *
 * @memberOf admin.studies.components.collectionSpecimenDescriptionSummary
 *
 * @param {domain.studies.CollectionSpecimenDescription} specimenDescription - the *Specimen Description* to
 * display information for.
 */
const collectionSpecimenDescriptionSummaryComponent = {
  template: require('./collectionSpecimenDescriptionSummary.html'),
  controller: CollectionSpecimenDescriptionSummaryController,
  controllerAs: 'vm',
  bindings: {
    specimenDescription: '<'
  }
};

export default ngModule => ngModule.component('collectionSpecimenDescriptionSummary',
                                             collectionSpecimenDescriptionSummaryComponent)
