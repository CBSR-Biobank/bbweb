/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var _ = require('lodash');

  var component = {
    templateUrl: '/assets/javascripts/collection/components/specimenView/specimenView.html',
    controller: SpecimenViewController,
    controllerAs: 'vm',
    bindings: {
      study:               '<',
      participant:         '<',
      collectionEventType: '<',
      collectionEvent:     '<',
      specimen:            '<'
    }
  };

  SpecimenViewController.$inject = ['$state', 'gettextCatalog', 'breadcrumbService'];

  /*
   * Displays the details for a single specimen and also allows the user to update certain fields.
   */
  function SpecimenViewController($state, gettextCatalog, breadcrumbService) {
    var vm = this;

    vm.specimenDescription = _.find(vm.collectionEventType.specimenDescriptions,
                                    { id: vm.specimen.specimenDescriptionId });

    vm.editParticipant = editParticipant;
    vm.back            = back;

    vm.breadcrumbs = [
      breadcrumbService.forState('home'),
      breadcrumbService.forState('home.collection'),
      breadcrumbService.forStateWithFunc(
        'home.collection.study',
        function () { return vm.study.name; }),
      breadcrumbService.forStateWithFunc(
        'home.collection.study.participant.cevents',
        function () {
          return gettextCatalog.getString('Participant {{uniqueId}}',
                                          { uniqueId: vm.participant.uniqueId });
        }),
      breadcrumbService.forStateWithFunc(
        'home.collection.study.participant.cevents.details',
        function () {
          return gettextCatalog.getString('Visit # {{vnumber}}',
                                          { vnumber: vm.collectionEvent.visitNumber });
        }),
      breadcrumbService.forStateWithFunc(
        'home.collection.study.participant.cevents.details.specimen',
        function () { return vm.specimen.inventoryId; })
    ];

    //---

    function editParticipant() {
      console.log(vm.participant);
    }

    function back() {
      $state.go('^');
    }
  }

  return component;
});
