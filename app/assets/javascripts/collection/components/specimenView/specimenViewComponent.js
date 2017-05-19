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

  SpecimenViewController.$inject = ['$state'];

  /*
   * Displays the details for a single specimen and also allows the user to update certain fields.
   */
  function SpecimenViewController($state) {
    var vm = this;

    vm.specimenDescription = _.find(vm.collectionEventType.specimenDescriptions,
                                    { id: vm.specimen.specimenDescriptionId });

    vm.editParticipant = editParticipant;
    vm.back            = back;

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
