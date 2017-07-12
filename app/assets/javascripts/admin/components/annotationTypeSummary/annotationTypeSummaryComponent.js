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
    templateUrl : '/assets/javascripts/admin/components/annotationTypeSummary/annotationTypeSummary.html',
    controller: AnnotationTypeSummaryController,
    controllerAs: 'vm',
    bindings: {
      annotationType: '<'
    }
  };

  AnnotationTypeSummaryController.$inject = [
    'gettextCatalog'
  ];

  function AnnotationTypeSummaryController(gettextCatalog) {
    var vm = this;
    vm.$onInit = onInit;

    //--

    function onInit() {
      vm.requiredLabel = vm.annotationType.required ?
        gettextCatalog.getString('Yes') : gettextCatalog.getString('No');
    }
  }

  return component;
});
