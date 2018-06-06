/**
 * AngularJS Component for {@link domain.studies.ProcessedEventType ProcessedEventType} administration.
 *
 * @namespace admin.studies.components.inputSpecimenProcessingSummary
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/**
 * An AngularJS component that displays summary information for a {@link
 * domain.studies.InputSpecimenProcessing InputSpecimenProcessing}
 *
 * @memberOf admin.studies.components.inputSpecimenProcessingSummary
 *
 * @param {domain.studies.InputSpecimenProcessing} specimenDefinition - the *Specimen Description* to
 * display information for.
 */
const inputSpecimenProcessingSummaryComponent = {
  template: require('./inputSpecimenProcessingSummary.html'),
  controllerAs: 'vm',
  bindings: {
    input:              '<',
    inputEntity:        '<',
    specimenDefinition: '<'
  }
};

export default ngModule => ngModule.component('inputSpecimenProcessingSummary',
                                             inputSpecimenProcessingSummaryComponent)
