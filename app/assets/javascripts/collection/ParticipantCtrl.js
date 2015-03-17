define([], function() {
  'use strict';

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

  return ParticipantCtrl;
});
