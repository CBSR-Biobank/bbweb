/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  /*
   * Displays a summary for the annotation type.
   */
  var component = {
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

  return component;
});
