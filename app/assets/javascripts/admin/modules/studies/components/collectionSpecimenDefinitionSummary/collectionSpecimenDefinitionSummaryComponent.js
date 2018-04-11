/**
 * AngularJS Component for {@link domain.studies.CollectionEventType CollectionEventType} administration.
 *
 * @namespace admin.studies.components.collectionSpecimenDefinitionSummary
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/*
 * Controller for this component.
 */
function CollectionSpecimenDefinitionSummaryController() {

}

/**
 * An AngularJS component that displays summary information for a {@link
 * domain.studies.CollectionSpecimenDefinition CollectionSpecimenDefinition}
 *
 * @memberOf admin.studies.components.collectionSpecimenDefinitionSummary
 *
 * @param {domain.studies.CollectionSpecimenDefinition} specimenDefinition - the *Specimen Description* to
 * display information for.
 */
const collectionSpecimenDefinitionSummaryComponent = {
  template: require('./collectionSpecimenDefinitionSummary.html'),
  controller: CollectionSpecimenDefinitionSummaryController,
  controllerAs: 'vm',
  bindings: {
    specimenDefinition: '<'
  }
};

export default ngModule => ngModule.component('collectionSpecimenDefinitionSummary',
                                             collectionSpecimenDefinitionSummaryComponent)
