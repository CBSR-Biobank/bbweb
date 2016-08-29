/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  var component = {
    templateUrl : '/assets/javascripts/admin/components/annotationTypeSummary/annotationTypeSummary.html',
    controller: AnnotationTypeSummaryController,
    controllerAs: 'vm',
    bindings: {
      annotationType: '=',
      test: '@'
    }
  };

  AnnotationTypeSummaryController.$inject = [
    'gettextCatalog'
  ];

  /**
   *
   */
  function AnnotationTypeSummaryController(gettextCatalog) {
    var vm = this;

    vm.requiredLabel = vm.annotationType.required ? gettextCatalog.getString('Yes') : gettextCatalog.getString('No');
  }

  return component;
});
