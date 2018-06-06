/**
 * AngularJS Component for {@link domain.studies.ProcessedEventType ProcessedEventType} administration.
 *
 * @namespace admin.studies.components.outputSpecimenProcessingSummary
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/**
 * An AngularJS component that displays summary information for a {@link
 * domain.studies.OutputSpecimenProcessing OutputSpecimenProcessing}
 *
 * @memberOf admin.studies.components.outputSpecimenProcessingSummary
 *
 * @param {domain.studies.OutputSpecimenProcessing} specimenDefinition - the *Specimen Description* to
 * display information for.
 */
const outputSpecimenProcessingSummaryComponent = {
  template: require('./outputSpecimenProcessingSummary.html'),
  controllerAs: 'vm',
  bindings: {
    output: '<'
  }
};

export default ngModule => ngModule.component('outputSpecimenProcessingSummary',
                                             outputSpecimenProcessingSummaryComponent)
