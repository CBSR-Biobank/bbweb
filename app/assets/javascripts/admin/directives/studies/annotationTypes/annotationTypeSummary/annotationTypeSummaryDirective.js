/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  /**
   *
   */
  function annotationTypeSummaryDirective() {
    var directive = {
      restrict: 'E',
      scope: {
        annotationType: '='
      },
      templateUrl : '/assets/javascripts/admin/directives/studies/annotationTypes/annotationTypeSummary/annotationTypeSummary.html'
    };

    return directive;
  }

  return annotationTypeSummaryDirective;

});
