/**
 * AngularJS Components used in Administration modules
 *
 * @namespace admin.common.components.annotationTypeSummary
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/**
 * An AngularJS component that displays summary information about an {@link domain.annotations.AnnotationType} in
 * a list item within a Bootstrap panel.
 *
 * @memberOf admin.common.components.annotationTypeSummary
 *
 * @param {domain.annotations.AnnotationType} annotationType the Annotation type summary to display.
 */
const annotationTypeSummaryComponent = {
  template: require('./annotationTypeSummary.html'),
  controller: AnnotationTypeSummaryController,
  controllerAs: 'vm',
  bindings: {
    annotationType: '<'
  }
};

function AnnotationTypeSummaryController() {
  var vm = this;
  vm.$onInit = onInit;

  //--

  function onInit() {
  }
}

export default ngModule => ngModule.component('annotationTypeSummary', annotationTypeSummaryComponent)
