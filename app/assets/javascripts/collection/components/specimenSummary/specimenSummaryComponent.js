/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  //SpecimenSummaryController.$inject = [];

  /**
   *
   */
  function SpecimenSummaryController() {
    // var vm = this;
    // console.log(vm.specimen);
  }

  return {
    templateUrl : '/assets/javascripts/collection/components/specimenSummary/specimenSummary.html',
    controller: SpecimenSummaryController,
    controllerAs: 'vm',
    bindings: {
      specimen: '<'
    }
  };
});
