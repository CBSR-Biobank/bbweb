/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */

/**
 * @class ng.admin.common.components.annotationTypeSummary
 *
 * An AngularJS component that displays summary information about an {@link domain.AnnotationType} in
 * a list item within a Bootstrap panel.
 *
 * @memberOf ng.admin.common.components
 *
 * @param {domain.AnnotationType} annotationType the Annotation type summary to display.
 */
var annotationTypeSummary = {
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

export default ngModule => ngModule.component('annotationTypeSummary', annotationTypeSummary)
