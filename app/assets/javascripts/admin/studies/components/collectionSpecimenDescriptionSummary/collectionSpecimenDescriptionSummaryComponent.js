/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  var component = {
    templateUrl : '/assets/javascripts/admin/studies/components/collectionSpecimenDescriptionSummary/collectionSpecimenDescriptionSummary.html',
    controller: CollectionSpecimenDescriptionSummaryController,
    controllerAs: 'vm',
    bindings: {
    }
  };

  CollectionSpecimenDescriptionSummaryController.$inject = [];

  /*
   * Controller for this component.
   */
  function CollectionSpecimenDescriptionSummaryController() {

  }

  return component;
});
