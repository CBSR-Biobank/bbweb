define(['./module'], function(module) {
  'use strict';

  module.controller('ParticipantCtrl', ParticipantCtrl);

  ParticipantCtrl.$inject = [
    'Participant',
    'study',
    'participant',
    'annotationTypes'
  ];

  /**
   *
   */
  function ParticipantCtrl(Participant,
                           study,
                           participant,
                           annotationTypes) {
    var vm = this;
    vm.participant = new Participant(study, participant, annotationTypes);
  }

});
