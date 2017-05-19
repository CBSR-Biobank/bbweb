/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  /**
   *
   */
  function collectionSpecimenDescriptionSummaryDirective() {
    var directive = {
      restrict: 'E',
      scope: {
        specimenDescription: '='
      },
      templateUrl : '/assets/javascripts/admin/studies/directives/collection/collectionSpecimenDescriptionSummary/collectionSpecimenDescriptionSummary.html'
    };

    return directive;
  }

  return collectionSpecimenDescriptionSummaryDirective;
});
