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
    'gettext'
  ];

  /**
   *
   */
  function AnnotationTypeSummaryController(gettext) {
    var vm = this;

    vm.requiredLabel = vm.annotationType.required ? gettext('Yes') : gettext('No');
  }

  return component;
});
