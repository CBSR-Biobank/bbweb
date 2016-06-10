/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  /**
   *
   */
  function collectionSpecimenSpecSummaryDirective() {
    var directive = {
      restrict: 'E',
      scope: {
        specimenSpec: '='
      },
      templateUrl : '/assets/javascripts/admin/studies/directives/collection/collectionSpecimenSpecSummary/collectionSpecimenSpecSummary.html'
    };

    return directive;
  }

  return collectionSpecimenSpecSummaryDirective;
});
